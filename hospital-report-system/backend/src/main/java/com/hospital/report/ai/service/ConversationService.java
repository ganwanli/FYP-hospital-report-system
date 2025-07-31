package com.hospital.report.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.report.ai.entity.AIConversation;
import com.hospital.report.ai.entity.AIMessage;
import com.hospital.report.ai.entity.dto.ConversationStats;
import com.hospital.report.ai.enums.ConversationStatus;
import com.hospital.report.ai.enums.MessageType;
import com.hospital.report.ai.mapper.AIConversationMapper;
import com.hospital.report.ai.mapper.AIMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class ConversationService {
    
    private final AIConversationMapper conversationMapper;
    private final AIMessageMapper messageMapper;
    
    public ConversationService(AIConversationMapper conversationMapper,
                              AIMessageMapper messageMapper) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
    }
    
    public AIConversation createConversation(Long userId, String title, Long datasourceId) {
        if (!StringUtils.hasText(title)) {
            title = "新对话 - " + LocalDateTime.now().toString().substring(0, 16);
        }
        
        AIConversation conversation = new AIConversation();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversation.setDatasourceId(datasourceId);
        conversation.setStatus(ConversationStatus.ACTIVE);
        conversation.setCreatedTime(LocalDateTime.now());
        conversation.setUpdatedTime(LocalDateTime.now());
        
        conversationMapper.insert(conversation);
        
        log.info("创建新对话成功，ID: {}, 用户: {}, 标题: {}", conversation.getId(), userId, title);
        return conversation;
    }
    
    public AIConversation getConversation(Long conversationId) {
        AIConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new RuntimeException("对话不存在，ID: " + conversationId);
        }
        return conversation;
    }
    
    public List<AIConversation> getUserConversations(Long userId, Integer limit) {
        QueryWrapper<AIConversation> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("status", ConversationStatus.ACTIVE)
               .orderByDesc("updated_time");
        
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        
        List<AIConversation> conversations = conversationMapper.selectList(wrapper);
        
        // 为每个对话添加统计信息
        for (AIConversation conversation : conversations) {
            enrichConversationWithStats(conversation);
        }
        
        return conversations;
    }
    
    public List<Map<String, Object>> getUserConversationsWithStats(Long userId, Integer limit) {
        return conversationMapper.selectConversationsWithStats(userId, ConversationStatus.ACTIVE.name());
    }
    
    public AIMessage saveMessage(Long conversationId, MessageType messageType, String content) {
        return saveMessage(conversationId, messageType, content, null, null);
    }
    
    public AIMessage saveMessage(Long conversationId, MessageType messageType, String content, String metadata, Integer tokenCount) {
        AIMessage message = new AIMessage();
        message.setConversationId(conversationId);
        message.setMessageType(messageType);
        message.setContent(content);
        message.setMetadata(metadata);
        message.setTokenCount(tokenCount != null ? tokenCount : 0);
        message.setCreatedTime(LocalDateTime.now());
        
        messageMapper.insert(message);
        
        // 更新对话的最后更新时间
        updateConversationTimestamp(conversationId);
        
        log.debug("保存消息成功，对话ID: {}, 类型: {}, 长度: {}", conversationId, messageType, content.length());
        return message;
    }
    
    public List<AIMessage> getRecentMessages(Long conversationId, Integer limit) {
        QueryWrapper<AIMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("conversation_id", conversationId)
               .orderByDesc("created_time");
        
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        
        List<AIMessage> messages = messageMapper.selectList(wrapper);
        Collections.reverse(messages); // 按时间正序返回
        return messages;
    }
    
    public List<AIMessage> getConversationMessages(Long conversationId) {
        return getRecentMessages(conversationId, null);
    }
    
    public void updateTokenUsage(Long conversationId, Integer tokenCount) {
        // 更新最后一条助手消息的token数
        QueryWrapper<AIMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("conversation_id", conversationId)
               .eq("message_type", MessageType.ASSISTANT)
               .orderByDesc("created_time")
               .last("LIMIT 1");
        
        AIMessage lastMessage = messageMapper.selectOne(wrapper);
        if (lastMessage != null) {
            lastMessage.setTokenCount(tokenCount);
            messageMapper.updateById(lastMessage);
            log.debug("更新Token使用量: {}, 消息ID: {}", tokenCount, lastMessage.getId());
        }
    }
    
    public void updateConversationTitle(Long conversationId, String newTitle) {
        AIConversation conversation = new AIConversation();
        conversation.setId(conversationId);
        conversation.setTitle(newTitle);
        conversation.setUpdatedTime(LocalDateTime.now());
        
        conversationMapper.updateById(conversation);
        log.info("更新对话标题: {}, 新标题: {}", conversationId, newTitle);
    }
    
    public void archiveConversation(Long conversationId) {
        AIConversation conversation = new AIConversation();
        conversation.setId(conversationId);
        conversation.setStatus(ConversationStatus.ARCHIVED);
        conversation.setUpdatedTime(LocalDateTime.now());
        
        conversationMapper.updateById(conversation);
        log.info("归档对话: {}", conversationId);
    }
    
    public void deleteConversation(Long conversationId) {
        // 软删除
        AIConversation conversation = new AIConversation();
        conversation.setId(conversationId);
        conversation.setStatus(ConversationStatus.DELETED);
        conversation.setUpdatedTime(LocalDateTime.now());
        
        conversationMapper.updateById(conversation);
        log.info("删除对话: {}", conversationId);
    }
    
    public void permanentlyDeleteConversation(Long conversationId) {
        // 物理删除消息
        QueryWrapper<AIMessage> messageWrapper = new QueryWrapper<>();
        messageWrapper.eq("conversation_id", conversationId);
        messageMapper.delete(messageWrapper);
        
        // 物理删除对话
        conversationMapper.deleteById(conversationId);
        log.info("永久删除对话: {}", conversationId);
    }
    
    public ConversationStats getConversationStats(Long userId) {
        ConversationStats stats = new ConversationStats();
        
        // 统计总对话数
        QueryWrapper<AIConversation> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("user_id", userId);
        stats.setTotalConversations(conversationMapper.selectCount(totalWrapper));
        
        // 统计活跃对话数
        QueryWrapper<AIConversation> activeWrapper = new QueryWrapper<>();
        activeWrapper.eq("user_id", userId).eq("status", ConversationStatus.ACTIVE);
        stats.setActiveConversations(conversationMapper.selectCount(activeWrapper));
        
        // 统计今日对话数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Long todayCount = conversationMapper.countByUserIdAndCreatedTimeAfter(userId, todayStart);
        stats.setTodayConversations(todayCount);
        
        // 统计总消息数和Token使用量
        Long totalMessages = messageMapper.getTotalMessagesByUserId(userId);
        stats.setTotalMessages(totalMessages != null ? totalMessages : 0L);
        
        Long totalTokens = messageMapper.getTotalTokensByUserId(userId);
        stats.setTotalTokensUsed(totalTokens != null ? totalTokens : 0L);
        
        // 计算平均Token使用量
        if (stats.getTotalMessages() > 0) {
            stats.setAverageTokensPerMessage((double) stats.getTotalTokensUsed() / stats.getTotalMessages());
        }
        
        return stats;
    }
    
    public void cleanupOldConversations(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        
        QueryWrapper<AIConversation> wrapper = new QueryWrapper<>();
        wrapper.eq("status", ConversationStatus.DELETED)
               .lt("updated_time", cutoffDate);
        
        List<AIConversation> oldConversations = conversationMapper.selectList(wrapper);
        
        for (AIConversation conversation : oldConversations) {
            permanentlyDeleteConversation(conversation.getId());
        }
        
        log.info("清理了 {} 个过期对话", oldConversations.size());
    }
    
    public String generateConversationTitle(String firstMessage) {
        if (!StringUtils.hasText(firstMessage)) {
            return "新对话";
        }
        
        // 简单的标题生成逻辑
        String title = firstMessage.length() > 30 ? 
            firstMessage.substring(0, 30) + "..." : firstMessage;
        
        // 移除换行符和多余空格
        title = title.replaceAll("\\s+", " ").trim();
        
        return title;
    }
    
    private void updateConversationTimestamp(Long conversationId) {
        AIConversation conversation = new AIConversation();
        conversation.setId(conversationId);
        conversation.setUpdatedTime(LocalDateTime.now());
        conversationMapper.updateById(conversation);
    }
    
    private void enrichConversationWithStats(AIConversation conversation) {
        // 获取消息数量
        QueryWrapper<AIMessage> messageWrapper = new QueryWrapper<>();
        messageWrapper.eq("conversation_id", conversation.getId());
        Long messageCount = messageMapper.selectCount(messageWrapper);
        conversation.setMessageCount(messageCount.intValue());
        
        // 获取最后一条消息
        QueryWrapper<AIMessage> lastMessageWrapper = new QueryWrapper<>();
        lastMessageWrapper.eq("conversation_id", conversation.getId())
                          .orderByDesc("created_time")
                          .last("LIMIT 1");
        AIMessage lastMessage = messageMapper.selectOne(lastMessageWrapper);
        if (lastMessage != null) {
            String content = lastMessage.getContent();
            conversation.setLastMessage(content.length() > 50 ? content.substring(0, 50) + "..." : content);
        }
    }
}