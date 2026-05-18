package com.example.realtime_message_application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.example.realtime_message_application.component.RateLimitingInterceptor;
import com.example.realtime_message_application.dto.conversation.AddParticipant;
import com.example.realtime_message_application.dto.conversation.ArchiveConv;
import com.example.realtime_message_application.dto.conversation.ConversationDTO;
import com.example.realtime_message_application.dto.conversation.ConversationResponse;
import com.example.realtime_message_application.dto.conversation.FavoriteConv;
import com.example.realtime_message_application.dto.conversation.LeaveConversation;
import com.example.realtime_message_application.dto.conversation.MuteConv;
import com.example.realtime_message_application.dto.conversation.RemoveParticipant;
import com.example.realtime_message_application.dto.conversation.UnMuteConv;
import com.example.realtime_message_application.dto.conversation.UpdateConvDescription;
import com.example.realtime_message_application.dto.conversation.UpdateConvImage;
import com.example.realtime_message_application.dto.conversation.UpdateConvRole;
import com.example.realtime_message_application.dto.conversation.UpdateConvTitle;
import com.example.realtime_message_application.enums.ConversationType;
import com.example.realtime_message_application.enums.ParticipantRole;
import com.example.realtime_message_application.security.JwtAuthenticationFilter;
import com.example.realtime_message_application.security.JwtService;
import com.example.realtime_message_application.service.ConversationService;
import com.example.realtime_message_application.service.RateLimitingService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ConversationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConversationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ConversationService conversationService;

        @MockBean
        private JpaMetamodelMappingContext jpaMetamodelMappingContext;

        @MockBean
        private RateLimitingService rateLimitingService;

        @MockBean
        private RateLimitingInterceptor rateLimitingInterceptor;

        @MockBean
        private JwtService jwtService;

        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @Autowired
        private ObjectMapper objectMapper;

        private ConversationResponse conversationResponse;

        @BeforeEach
        void setUp() {
                conversationResponse = new ConversationResponse(
                                1L,
                                "Test Conv",
                                "convCreatorName",
                                "Description",
                                ConversationType.GROUP,
                                "avatar.png",
                                Instant.now(),
                                List.of());
        }

        @Test
        void getAllConversation_ShouldReturnList() throws Exception {
                when(conversationService.getAllConversations()).thenReturn(List.of(conversationResponse));

                mockMvc.perform(get("/api/conversation/all"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.size()").value(1));
        }

        @Test
        void getAllConversationByUserId_ShouldReturnList() throws Exception {
                when(conversationService.getAllConversationByUserId(1L)).thenReturn(List.of(conversationResponse));

                mockMvc.perform(get("/api/conversation/all/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.size()").value(1));
        }

        @Test
        void getConversationById_ShouldReturnConversation() throws Exception {
                when(conversationService.getConversationById(1L)).thenReturn(conversationResponse);

                mockMvc.perform(get("/api/conversation/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Test Conv"));
        }

        @Test
        void createConversation_ShouldReturnCreated() throws Exception {
                ConversationDTO dto = new ConversationDTO("Test", "desc", ConversationType.GROUP, 1L, Set.of());
                MockMultipartFile conversationPart = new MockMultipartFile("conversation", "conversation",
                                "application/json",
                                objectMapper.writeValueAsString(dto).getBytes());
                MockMultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", "dummy".getBytes());

                when(conversationService.createConversation(any(), any())).thenReturn(conversationResponse);

                mockMvc.perform(multipart("/api/conversation/create")
                                .file(conversationPart)
                                .file(image))
                                .andExpect(status().isOk());
        }

        @Test
        void getAllMessagesByConversationId_ShouldReturnList() throws Exception {
                when(conversationService.getAllMessagesByConversationId(1L)).thenReturn(List.of(100L, 200L));

                mockMvc.perform(get("/api/conversation/all/messages/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.size()").value(2));
        }

        @Test
        void removeAllConversationByUserId_ShouldReturnSuccess() throws Exception {
                mockMvc.perform(delete("/api/conversation/all/1"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("All conversations removed successfully."));
        }

        @Test
        void removeConversationById_ShouldReturnSuccess() throws Exception {
                mockMvc.perform(delete("/api/conversation/1"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Conversation removed successfully."));
        }

        @Test
        void removeAllMessageInConversation_ShouldReturnSuccess() throws Exception {
                mockMvc.perform(delete("/api/conversation/all/messages/1"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("All messages in conversation removed successfully."));
        }

        @Test
        void addParticipantInConversation_ShouldReturnOk() throws Exception {
                AddParticipant addParticipant = new AddParticipant(1L, 2L, 1L, null);
                when(conversationService.AddParticipantInConversation(addParticipant)).thenReturn(conversationResponse);

                mockMvc.perform(post("/api/conversation/add/participant")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(addParticipant)))
                                .andExpect(status().isOk());
        }

        @Test
        void removeParticipantInConversation_ShouldReturnOk() throws Exception {
                RemoveParticipant removeParticipant = new RemoveParticipant(1L, 2L, 1L);
                when(conversationService.removeParticipantInConversation(removeParticipant))
                                .thenReturn(conversationResponse);

                mockMvc.perform(post("/api/conversation/remove/participant")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(removeParticipant)))
                                .andExpect(status().isOk());
        }

        @Test
        void leaveConversation_ShouldReturnOk() throws Exception {
                LeaveConversation leaveConversation = new LeaveConversation(1L, 2L);
                when(conversationService.leaveConversation(leaveConversation)).thenReturn("User left the conversation");

                mockMvc.perform(post("/api/conversation/leave")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(leaveConversation)))
                                .andExpect(status().isOk());
        }

        @Test
        void updateConversationTitle_ShouldReturnOk() throws Exception {
                UpdateConvTitle updateConvTitle = new UpdateConvTitle(1L, "New Title");
                when(conversationService.updateConversationTitle(updateConvTitle)).thenReturn(conversationResponse);

                mockMvc.perform(post("/api/conversation/update/title")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateConvTitle)))
                                .andExpect(status().isOk());
        }

        @Test
        void updateConversationImage_ShouldReturnOk() throws Exception {
                UpdateConvImage dto = new UpdateConvImage(1L, 2L, null);
                MockMultipartFile updatePart = new MockMultipartFile("updateConvImage", "updateConvImage",
                                "application/json",
                                objectMapper.writeValueAsString(dto).getBytes());
                MockMultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", "dummy".getBytes());

                when(conversationService.updateConversationImage(any(UpdateConvImage.class)))
                                .thenReturn(conversationResponse);

                mockMvc.perform(multipart("/api/conversation/update/image")
                                .file(updatePart)
                                .file(image))
                                .andExpect(status().isOk());
        }

        @Test
        void updateConversationRole_ShouldReturnOk() throws Exception {
                UpdateConvRole updateConvRole = new UpdateConvRole(1L, 2L, ParticipantRole.ADMIN, 1L);
                when(conversationService.updateConversationRole(updateConvRole)).thenReturn(conversationResponse);

                mockMvc.perform(post("/api/conversation/update/role")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateConvRole)))
                                .andExpect(status().isOk());
        }

        @Test
        void updateConversationDescription_ShouldReturnOk() throws Exception {
                UpdateConvDescription updateConvDescription = new UpdateConvDescription(1L, "New Desc");
                when(conversationService.updateConversationDescription(updateConvDescription))
                                .thenReturn(conversationResponse);

                mockMvc.perform(post("/api/conversation/update/description")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateConvDescription)))
                                .andExpect(status().isOk());
        }

        @Test
        void muteConversation_ShouldReturnSuccess() throws Exception {
                MuteConv muteConv = new MuteConv(1L, 1L, 60);
                mockMvc.perform(post("/api/conversation/mute")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(muteConv)))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Conversation muted successfully."));
        }

        @Test
        void unMuteConversation_ShouldReturnSuccess() throws Exception {
                UnMuteConv unMuteConv = new UnMuteConv(1L, 1L);
                mockMvc.perform(post("/api/conversation/unmute")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unMuteConv)))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Conversation unmuted successfully."));
        }

        @Test
        void archiveConversation_ShouldReturnSuccess() throws Exception {
                ArchiveConv archiveConv = new ArchiveConv(1L, 1L);
                when(conversationService.addOrRemoveAsArchive(archiveConv)).thenReturn(conversationResponse);

                mockMvc.perform(post("/api/conversation/archive")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(archiveConv)))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Conversation archived successfully."));
        }

        @Test
        void favoriteConversation_ShouldReturnSuccess() throws Exception {
                FavoriteConv favoriteConv = new FavoriteConv(1L, 1L);
                when(conversationService.addOrRemoveAsFavorites(favoriteConv)).thenReturn(conversationResponse);

                mockMvc.perform(post("/api/conversation/favorite")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(favoriteConv)))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Conversation favorited successfully."));
        }

        @Test
        void getAllFavoriteConversation_ShouldReturnList() throws Exception {
                when(conversationService.getAllFavoriteConversation(1L)).thenReturn(List.of(conversationResponse));

                mockMvc.perform(get("/api/conversation/all/favorite/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.size()").value(1));
        }

        @Test
        void getAllArchivedConversation_ShouldReturnList() throws Exception {
                when(conversationService.getAllArchivedConversation(1L)).thenReturn(List.of(conversationResponse));

                mockMvc.perform(get("/api/conversation/all/archive/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.size()").value(1));
        }
}
