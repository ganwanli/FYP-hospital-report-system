package com.hospital.report.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.ai.entity.AIConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AIConversationMapper extends BaseMapper<AIConversation> {
    
    @Select("SELECT c.*, " +
            "(SELECT COUNT(*) FROM ai_message m WHERE m.conversation_id = c.id) as message_count, " +
            "(SELECT content FROM ai_message m WHERE m.conversation_id = c.id ORDER BY created_time DESC LIMIT 1) as last_message " +
            "FROM ai_conversation c WHERE c.user_id = #{userId} AND c.status = #{status} " +
            "ORDER BY c.updated_time DESC")
    List<Map<String, Object>> selectConversationsWithStats(@Param("userId") Long userId, @Param("status") String status);
    
    @Select("SELECT COUNT(*) FROM ai_conversation WHERE user_id = #{userId} AND created_time >= #{startTime}")
    Long countByUserIdAndCreatedTimeAfter(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);
}