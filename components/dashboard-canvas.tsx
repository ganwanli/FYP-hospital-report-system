"use client"

import React, { useState, useRef, useCallback } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { 
  Edit3, 
  Type, 
  Palette, 
  Move, 
  Trash2, 
  Save,
  X,
  Bold,
  Italic,
  Underline,
  AlignLeft,
  AlignCenter,
  AlignRight
} from 'lucide-react'

interface CanvasElement {
  id: string
  type: 'text' | 'report'
  x: number
  y: number
  width: number
  height: number
  content?: string
  fontSize?: number
  fontWeight?: string
  fontFamily?: string
  color?: string
  textAlign?: string
  backgroundColor?: string
  reportData?: any
}

interface DashboardCanvasProps {
  language: "zh-CN" | "en-US"
  theme: any
}

export default function DashboardCanvas({ language, theme }: DashboardCanvasProps) {
  const [elements, setElements] = useState<CanvasElement[]>([])
  const [selectedElement, setSelectedElement] = useState<string | null>(null)
  const [isEditing, setIsEditing] = useState<string | null>(null)
  const [draggedElement, setDraggedElement] = useState<string | null>(null)
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 })
  const [isResizing, setIsResizing] = useState<string | null>(null)
  const [showTextEditor, setShowTextEditor] = useState(false)
  const canvasRef = useRef<HTMLDivElement>(null)

  const getText = (zh: string, en: string) => (language === "zh-CN" ? zh : en)

  // 处理拖拽放置
  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    const rect = canvasRef.current?.getBoundingClientRect()
    if (!rect) return

    const x = e.clientX - rect.left
    const y = e.clientY - rect.top

    try {
      const data = JSON.parse(e.dataTransfer.getData('application/json'))
      
      if (data.type === 'report') {
        const newElement: CanvasElement = {
          id: `element-${Date.now()}`,
          type: 'report',
          x: Math.max(0, x - 100),
          y: Math.max(0, y - 50),
          width: 300,
          height: 200,
          reportData: data.data
        }
        setElements(prev => [...prev, newElement])
      }
    } catch (error) {
      console.error('Error parsing drop data:', error)
    }
  }, [])

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault()
  }, [])

  // 添加文本元素
  const addTextElement = () => {
    const newElement: CanvasElement = {
      id: `text-${Date.now()}`,
      type: 'text',
      x: 50,
      y: 50,
      width: 200,
      height: 40,
      content: getText('点击编辑文本', 'Click to edit text'),
      fontSize: 16,
      fontWeight: 'normal',
      fontFamily: 'inherit',
      color: '#000000',
      textAlign: 'left',
      backgroundColor: 'transparent'
    }
    setElements(prev => [...prev, newElement])
    setSelectedElement(newElement.id)
    setIsEditing(newElement.id)
  }

  // 开始拖拽元素
  const handleMouseDown = (e: React.MouseEvent, elementId: string) => {
    if (isEditing) return
    
    const rect = canvasRef.current?.getBoundingClientRect()
    if (!rect) return

    const element = elements.find(el => el.id === elementId)
    if (!element) return

    setDraggedElement(elementId)
    setSelectedElement(elementId)
    setDragOffset({
      x: e.clientX - rect.left - element.x,
      y: e.clientY - rect.top - element.y
    })
  }

  // 处理鼠标移动
  const handleMouseMove = useCallback((e: MouseEvent) => {
    if (!draggedElement || !canvasRef.current) return

    const rect = canvasRef.current.getBoundingClientRect()
    const newX = Math.max(0, e.clientX - rect.left - dragOffset.x)
    const newY = Math.max(0, e.clientY - rect.top - dragOffset.y)

    setElements(prev => prev.map(el => 
      el.id === draggedElement 
        ? { ...el, x: newX, y: newY }
        : el
    ))
  }, [draggedElement, dragOffset])

  // 处理鼠标释放
  const handleMouseUp = useCallback(() => {
    setDraggedElement(null)
    setIsResizing(null)
  }, [])

  // 绑定全局鼠标事件
  React.useEffect(() => {
    if (draggedElement || isResizing) {
      document.addEventListener('mousemove', handleMouseMove)
      document.addEventListener('mouseup', handleMouseUp)
      return () => {
        document.removeEventListener('mousemove', handleMouseMove)
        document.removeEventListener('mouseup', handleMouseUp)
      }
    }
  }, [draggedElement, isResizing, handleMouseMove, handleMouseUp])

  // 删除元素
  const deleteElement = (elementId: string) => {
    setElements(prev => prev.filter(el => el.id !== elementId))
    setSelectedElement(null)
    setIsEditing(null)
  }

  // 更新元素内容
  const updateElement = (elementId: string, updates: Partial<CanvasElement>) => {
    setElements(prev => prev.map(el => 
      el.id === elementId ? { ...el, ...updates } : el
    ))
  }

  // 开始编辑文本
  const startEditing = (elementId: string) => {
    setIsEditing(elementId)
    setSelectedElement(elementId)
  }

  // 结束编辑
  const finishEditing = () => {
    setIsEditing(null)
  }

  return (
    <Card className="h-full">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <CardTitle className="text-lg">
            {getText('可编辑显示区域', 'Editable Display Area')}
          </CardTitle>
          <div className="flex items-center gap-2">
            <Button 
              size="sm" 
              variant="outline" 
              onClick={addTextElement}
              className="flex items-center gap-1"
            >
              <Type className="w-4 h-4" />
              {getText('添加文本', 'Add Text')}
            </Button>
            <Button 
              size="sm" 
              variant="outline" 
              onClick={() => setShowTextEditor(!showTextEditor)}
              className="flex items-center gap-1"
            >
              <Edit3 className="w-4 h-4" />
              {getText('编辑工具', 'Edit Tools')}
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent className="p-0">
        {/* 编辑工具栏 */}
        {showTextEditor && selectedElement && (
          <div className="border-b p-4 bg-gray-50">
            <TextEditingToolbar 
              element={elements.find(el => el.id === selectedElement)}
              onUpdate={(updates) => updateElement(selectedElement, updates)}
              language={language}
            />
          </div>
        )}

        {/* 画布区域 */}
        <div 
          ref={canvasRef}
          className="relative bg-white border-2 border-dashed border-gray-200 min-h-[500px] overflow-hidden"
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          onClick={() => {
            if (!isEditing) {
              setSelectedElement(null)
            }
          }}
        >
          {/* 提示文本 */}
          {elements.length === 0 && (
            <div className="absolute inset-0 flex items-center justify-center text-gray-400 pointer-events-none">
              <div className="text-center">
                <p className="text-lg font-medium">
                  {getText('拖拽报表到此处或点击添加文本', 'Drag reports here or click to add text')}
                </p>
                <p className="text-sm mt-2">
                  {getText('支持自由编辑和调整大小', 'Support free editing and resizing')}
                </p>
              </div>
            </div>
          )}

          {/* 渲染所有元素 */}
          {elements.map(element => (
            <CanvasElementRenderer
              key={element.id}
              element={element}
              isSelected={selectedElement === element.id}
              isEditing={isEditing === element.id}
              onMouseDown={(e) => handleMouseDown(e, element.id)}
              onDoubleClick={() => element.type === 'text' && startEditing(element.id)}
              onDelete={() => deleteElement(element.id)}
              onUpdate={(updates) => updateElement(element.id, updates)}
              onFinishEditing={finishEditing}
              language={language}
            />
          ))}
        </div>
      </CardContent>
    </Card>
  )
}

// 文本编辑工具栏组件
interface TextEditingToolbarProps {
  element?: CanvasElement
  onUpdate: (updates: Partial<CanvasElement>) => void
  language: "zh-CN" | "en-US"
}

function TextEditingToolbar({ element, onUpdate, language }: TextEditingToolbarProps) {
  const getText = (zh: string, en: string) => (language === "zh-CN" ? zh : en)

  if (!element || element.type !== 'text') return null

  return (
    <div className="flex items-center gap-4 flex-wrap">
      {/* 字体大小 */}
      <div className="flex items-center gap-2">
        <label className="text-sm font-medium">{getText('字号', 'Size')}:</label>
        <Select 
          value={element.fontSize?.toString() || '16'} 
          onValueChange={(value) => onUpdate({ fontSize: parseInt(value) })}
        >
          <SelectTrigger className="w-20">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="12">12px</SelectItem>
            <SelectItem value="14">14px</SelectItem>
            <SelectItem value="16">16px</SelectItem>
            <SelectItem value="18">18px</SelectItem>
            <SelectItem value="20">20px</SelectItem>
            <SelectItem value="24">24px</SelectItem>
            <SelectItem value="32">32px</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* 字体粗细 */}
      <div className="flex items-center gap-1">
        <Button
          size="sm"
          variant={element.fontWeight === 'bold' ? 'default' : 'outline'}
          onClick={() => onUpdate({ fontWeight: element.fontWeight === 'bold' ? 'normal' : 'bold' })}
        >
          <Bold className="w-4 h-4" />
        </Button>
      </div>

      {/* 文字颜色 */}
      <div className="flex items-center gap-2">
        <label className="text-sm font-medium">{getText('颜色', 'Color')}:</label>
        <input
          type="color"
          value={element.color || '#000000'}
          onChange={(e) => onUpdate({ color: e.target.value })}
          className="w-8 h-8 rounded border cursor-pointer"
        />
      </div>

      {/* 对齐方式 */}
      <div className="flex items-center gap-1">
        <Button
          size="sm"
          variant={element.textAlign === 'left' ? 'default' : 'outline'}
          onClick={() => onUpdate({ textAlign: 'left' })}
        >
          <AlignLeft className="w-4 h-4" />
        </Button>
        <Button
          size="sm"
          variant={element.textAlign === 'center' ? 'default' : 'outline'}
          onClick={() => onUpdate({ textAlign: 'center' })}
        >
          <AlignCenter className="w-4 h-4" />
        </Button>
        <Button
          size="sm"
          variant={element.textAlign === 'right' ? 'default' : 'outline'}
          onClick={() => onUpdate({ textAlign: 'right' })}
        >
          <AlignRight className="w-4 h-4" />
        </Button>
      </div>
    </div>
  )
}
