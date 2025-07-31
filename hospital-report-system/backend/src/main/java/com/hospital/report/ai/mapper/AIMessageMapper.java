package com.hospital.report.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.ai.entity.AIMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AIMessageMapper extends BaseMapper<AIMessage> {
    
    @Select("SELECT SUM(token_count) FROM ai_message WHERE conversation_id IN " +
            "(SELECT id FROM ai_conversation WHERE user_id = #{userId})")
    Long getTotalTokensByUserId(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM ai_message WHERE conversation_id IN " +
            "(SELECT id FROM ai_conversation WHERE user_id = #{userId})")
    Long getTotalMessagesByUserId(@Param("userId") Long userId);
}