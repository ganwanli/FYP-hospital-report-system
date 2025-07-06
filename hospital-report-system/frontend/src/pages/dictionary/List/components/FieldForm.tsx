import React, { useState, useEffect } from 'react'
import {
  Modal,
  Form,
  Input,
  Select,
  InputNumber,
  Switch,
  TreeSelect,
  Tabs,
  Row,
  Col,
  Card,
  Button,
  Space,
  Typography,
  Divider,
  Tag,
  message
} from 'antd'
import { CheckOutlined, InfoCircleOutlined } from '@ant-design/icons'
import { dictionaryAPI } from '@/services'
import type { DataDictionary } from '@/services'

const { Option } = Select
const { TextArea } = Input
const { TabPane } = Tabs
const { Text } = Typography

interface FieldFormProps {
  visible: boolean
  field: DataDictionary | null
  categories: any[]
  onSubmit: (values: DataDictionary) => void
  onCancel: () => void
}

const FieldForm: React.FC<FieldFormProps> = ({
  visible,
  field,
  categories,
  onSubmit,
  onCancel
}) => {
  const [form] = Form.useForm()
  const [validatingFieldCode, setValidatingFieldCode] = useState(false)
  const [fieldCodeValid, setFieldCodeValid] = useState<boolean | undefined>(undefined)

  useEffect(() => {
    if (visible && field) {
      form.setFieldsValue({
        ...field,
        tags: field.tags ? field.tags.split(',') : []
      })
    } else if (visible) {
      form.resetFields()
      form.setFieldsValue({
        dataType: 'VARCHAR',
        isNullable: true,
        status: 1,
        isStandard: false,
        approvalStatus: 'PENDING',
        version: '1.0'
      })
    }
    setFieldCodeValid(undefined)
  }, [visible, field, form])

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      
      // 处理标签
      if (values.tags && Array.isArray(values.tags)) {
        values.tags = values.tags.join(',')
      }
      
      onSubmit(values)
    } catch (error) {
      console.error('表单验证失败:', error)
    }
  }

  const validateFieldCode = async (fieldCode: string) => {
    if (!fieldCode || fieldCode.length < 2) {
      setFieldCodeValid(undefined)
      return
    }

    setValidatingFieldCode(true)
    try {
      const response = await dictionaryAPI.validateFieldCode(fieldCode, field?.id)
      if (response.code === 200) {
        setFieldCodeValid(response.data)
      }
    } catch (error) {
      console.error('验证字段编码失败:', error)
      setFieldCodeValid(undefined)
    } finally {
      setValidatingFieldCode(false)
    }
  }

  const dataTypes = [
    { value: 'VARCHAR', label: 'VARCHAR', desc: '可变长度字符串' },
    { value: 'CHAR', label: 'CHAR', desc: '固定长度字符串' },
    { value: 'TEXT', label: 'TEXT', desc: '长文本' },
    { value: 'INT', label: 'INT', desc: '整数' },
    { value: 'BIGINT', label: 'BIGINT', desc: '长整数' },
    { value: 'DECIMAL', label: 'DECIMAL', desc: '定点数' },
    { value: 'FLOAT', label: 'FLOAT', desc: '浮点数' },
    { value: 'DOUBLE', label: 'DOUBLE', desc: '双精度浮点数' },
    { value: 'DATE', label: 'DATE', desc: '日期' },
    { value: 'DATETIME', label: 'DATETIME', desc: '日期时间' },
    { value: 'TIMESTAMP', label: 'TIMESTAMP', desc: '时间戳' },
    { value: 'BOOLEAN', label: 'BOOLEAN', desc: '布尔值' },
    { value: 'JSON', label: 'JSON', desc: 'JSON对象' },
    { value: 'BLOB', label: 'BLOB', desc: '二进制大对象' }
  ]

  const updateFrequencies = [
    { value: 'REAL_TIME', label: '实时' },
    { value: 'DAILY', label: '每日' },
    { value: 'WEEKLY', label: '每周' },
    { value: 'MONTHLY', label: '每月' },
    { value: 'QUARTERLY', label: '每季度' },
    { value: 'YEARLY', label: '每年' },
    { value: 'ON_DEMAND', label: '按需' }
  ]

  const approvalStatuses = [
    { value: 'PENDING', label: '待审批', color: 'orange' },
    { value: 'APPROVED', label: '已审批', color: 'green' },
    { value: 'REJECTED', label: '已拒绝', color: 'red' }
  ]

  const commonTags = [
    '核心字段', '业务主键', '外键', '计算字段', '衍生字段',
    '敏感数据', 'PII', '必填字段', '唯一字段', '索引字段'
  ]

  return (
    <Modal
      title={field ? '编辑字段' : '新建字段'}
      open={visible}
      onCancel={onCancel}
      width={900}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          取消
        </Button>,
        <Button key="submit" type="primary" onClick={handleSubmit}>
          {field ? '更新' : '创建'}
        </Button>
      ]}
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          dataType: 'VARCHAR',
          isNullable: true,
          status: 1,
          isStandard: false,
          approvalStatus: 'PENDING',
          version: '1.0'
        }}
      >
        <Tabs defaultActiveKey="basic">
          <TabPane tab="基本信息" key="basic">
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  label="字段编码"
                  name="fieldCode"
                  rules={[
                    { required: true, message: '请输入字段编码' },
                    { pattern: /^[A-Za-z][A-Za-z0-9_]*$/, message: '字段编码必须以字母开头，只能包含字母、数字和下划线' }
                  ]}
                  validateStatus={
                    fieldCodeValid === false ? 'error' : 
                    fieldCodeValid === true ? 'success' : undefined
                  }
                  help={
                    fieldCodeValid === false ? '字段编码已存在' :
                    fieldCodeValid === true ? '字段编码可用' : undefined
                  }
                >
                  <Input
                    placeholder="请输入字段编码"
                    suffix={
                      fieldCodeValid === true ? (
                        <CheckOutlined style={{ color: '#52c41a' }} />
                      ) : undefined
                    }
                    onChange={(e) => {
                      const value = e.target.value
                      if (value && value.length >= 2) {
                        const timeoutId = setTimeout(() => {
                          validateFieldCode(value)
                        }, 500)
                        return () => clearTimeout(timeoutId)
                      }
                    }}
                  />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label="中文名称"
                  name="fieldNameCn"
                  rules={[{ required: true, message: '请输入中文名称' }]}
                >
                  <Input placeholder="请输入中文名称" />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  label="英文名称"
                  name="fieldNameEn"
                >
                  <Input placeholder="请输入英文名称" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label="字段分类"
                  name="categoryId"
                  rules={[{ required: true, message: '请选择字段分类' }]}
                >
                  <TreeSelect
                    treeData={categories}
                    placeholder="请选择字段分类"
                    treeDefaultExpandAll
                    allowClear
                  />
                </Form.Item>
              </Col>
            </Row>

            <Form.Item
              label="业务含义"
              name="businessMeaning"
              rules={[{ required: true, message: '请输入业务含义' }]}
            >
              <TextArea rows={3} placeholder="请详细描述字段的业务含义和用途" />
            </Form.Item>

            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="负责人"
                  name="ownerUser"
                  rules={[{ required: true, message: '请输入负责人' }]}
                >
                  <Input placeholder="请输入负责人" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="责任部门"
                  name="ownerDepartment"
                  rules={[{ required: true, message: '请输入责任部门' }]}
                >
                  <Input placeholder="请输入责任部门" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="审批状态"
                  name="approvalStatus"
                >
                  <Select placeholder="请选择审批状态">
                    {approvalStatuses.map(status => (
                      <Option key={status.value} value={status.value}>
                        <Tag color={status.color}>{status.label}</Tag>
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
            </Row>
          </TabPane>

          <TabPane tab="数据类型" key="datatype">
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="数据类型"
                  name="dataType"
                  rules={[{ required: true, message: '请选择数据类型' }]}
                >
                  <Select placeholder="请选择数据类型">
                    {dataTypes.map(type => (
                      <Option key={type.value} value={type.value}>
                        <div>
                          <div style={{ fontWeight: 500 }}>{type.label}</div>
                          <Text type="secondary" style={{ fontSize: 11 }}>
                            {type.desc}
                          </Text>
                        </div>
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="长度"
                  name="dataLength"
                >
                  <InputNumber
                    placeholder="字段长度"
                    min={1}
                    max={65535}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="是否可空"
                  name="isNullable"
                  valuePropName="checked"
                >
                  <Switch checkedChildren="是" unCheckedChildren="否" />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="精度"
                  name="dataPrecision"
                  tooltip="数值类型的总位数"
                >
                  <InputNumber
                    placeholder="精度"
                    min={1}
                    max={65}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="小数位数"
                  name="dataScale"
                  tooltip="小数点后的位数"
                >
                  <InputNumber
                    placeholder="小数位数"
                    min={0}
                    max={30}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="默认值"
                  name="defaultValue"
                >
                  <Input placeholder="默认值" />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  label="值域范围"
                  name="valueRange"
                  tooltip="字段可能的取值范围，如：0-100, A|B|C"
                >
                  <Input placeholder="如：0-100, A|B|C" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label="示例值"
                  name="sampleValues"
                  tooltip="用逗号分隔的示例值"
                >
                  <Input placeholder="示例值1,示例值2,示例值3" />
                </Form.Item>
              </Col>
            </Row>

            <Form.Item
              label="数据质量规则"
              name="dataQualityRules"
              tooltip="数据验证规则和约束条件"
            >
              <TextArea rows={3} placeholder="请描述数据质量规则，如格式要求、约束条件等" />
            </Form.Item>
          </TabPane>

          <TabPane tab="数据来源" key="datasource">
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  label="数据来源"
                  name="dataSource"
                  tooltip="数据来源系统或表名"
                >
                  <Input placeholder="如：用户管理系统.user_info" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label="更新频率"
                  name="updateFrequency"
                >
                  <Select placeholder="请选择更新频率">
                    {updateFrequencies.map(freq => (
                      <Option key={freq.value} value={freq.value}>
                        {freq.label}
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  label="源表名"
                  name="tableName"
                >
                  <Input placeholder="源表名" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label="源列名"
                  name="columnName"
                >
                  <Input placeholder="源列名" />
                </Form.Item>
              </Col>
            </Row>

            <Form.Item
              label="相关字段"
              name="relatedFields"
              tooltip="相关联的其他字段编码，用逗号分隔"
            >
              <Input placeholder="field_code1,field_code2,field_code3" />
            </Form.Item>
          </TabPane>

          <TabPane tab="标准化" key="standard">
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="是否标准字段"
                  name="isStandard"
                  valuePropName="checked"
                >
                  <Switch 
                    checkedChildren="是" 
                    unCheckedChildren="否"
                    onChange={(checked) => {
                      if (!checked) {
                        form.setFieldValue('standardReference', '')
                      }
                    }}
                  />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="版本号"
                  name="version"
                >
                  <Input placeholder="如：1.0" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="状态"
                  name="status"
                >
                  <Select>
                    <Option value={1}>启用</Option>
                    <Option value={0}>禁用</Option>
                  </Select>
                </Form.Item>
              </Col>
            </Row>

            <Form.Item dependencies={['isStandard']}>
              {({ getFieldValue }) => {
                const isStandard = getFieldValue('isStandard')
                return isStandard ? (
                  <Form.Item
                    label="标准参考"
                    name="standardReference"
                    rules={[{ required: true, message: '请输入标准参考' }]}
                  >
                    <Input placeholder="请输入参考的标准规范" />
                  </Form.Item>
                ) : null
              }}
            </Form.Item>

            <Form.Item
              label="字段标签"
              name="tags"
              tooltip="为字段添加标签，便于分类和搜索"
            >
              <Select
                mode="tags"
                placeholder="请选择或输入标签"
                style={{ width: '100%' }}
              >
                {commonTags.map(tag => (
                  <Option key={tag} value={tag}>{tag}</Option>
                ))}
              </Select>
            </Form.Item>

            <Form.Item
              label="备注"
              name="remark"
            >
              <TextArea rows={3} placeholder="其他备注信息" />
            </Form.Item>
          </TabPane>
        </Tabs>
      </Form>
    </Modal>
  )
}

export default FieldForm