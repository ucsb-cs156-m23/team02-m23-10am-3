package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;
import edu.ucsb.cs156.example.entities.RecRequest;
import edu.ucsb.cs156.example.repositories.RecRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecRequestController.class)
@Import(TestConfig.class)
public class RecRequestControllerTests extends ControllerTestCase {

        @MockBean
        UCSBDateRepository ucsbDateRepository;

        @MockBean
        RecRequestRepository recRequestRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/recommendationrequest/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/recommendationrequest/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/recommendationrequest/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/recommendationrequest?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/recommendationrequest/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/recommendationrequest/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/recommendationrequest/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                RecRequest recRequest = RecRequest.builder()
                                .requesterEmail("ryanhe@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("For BS/MS program")
                                .dateRequested(ldt)
                                .dateNeeded(ldt)
                                .done(true)
                                .build();

                when(recRequestRepository.findById(eq(7L))).thenReturn(Optional.of(recRequest));

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendationrequest?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recRequestRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(recRequest);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(recRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendationrequest?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(recRequestRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("RecRequest with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_recrequests() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                RecRequest recRequest1 = RecRequest.builder()
                                .requesterEmail("ryanhe@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("For BS/MS program")
                                .dateRequested(ldt1)
                                .dateNeeded(ldt1)
                                .done(true)
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                RecRequest recRequest2 = RecRequest.builder()
                                .requesterEmail("ryanhe@ucsb.edu")
                                .professorEmail("alexmei@ucsb.edu")
                                .explanation("For BS/MS program")
                                .dateRequested(ldt2)
                                .dateNeeded(ldt2)
                                .done(true)
                                .build();


                ArrayList<RecRequest> expectedRequests = new ArrayList<>();
                expectedRequests.addAll(Arrays.asList(recRequest1, recRequest2));

                when(recRequestRepository.findAll()).thenReturn(expectedRequests);

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendationrequest/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedRequests);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_recrequest() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                RecRequest recRequest1 = RecRequest.builder()
                                .requesterEmail("ryanhe@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("For BS/MS program")
                                .dateRequested(ldt1)
                                .dateNeeded(ldt1)
                                .done(true)
                                .build();


                when(recRequestRepository.save(eq(recRequest1))).thenReturn(recRequest1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/recommendationrequest/post?requesterEmail=ryanhe@ucsb.edu&professorEmail=phtcon@ucsb.edu&explanation=For BS/MS program&dateRequested=2022-01-03T00:00:00&dateNeeded=2022-01-03T00:00:00&done=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recRequestRepository, times(1)).save(recRequest1);
                String expectedJson = mapper.writeValueAsString(recRequest1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                RecRequest recRequest1 = RecRequest.builder()
                                .requesterEmail("ryanhe@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("For BS/MS program")
                                .dateRequested(ldt1)
                                .dateNeeded(ldt1)
                                .done(true)
                                .build();

                when(recRequestRepository.findById(eq(15L))).thenReturn(Optional.of(recRequest1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/recommendationrequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recRequestRepository, times(1)).findById(15L);
                verify(recRequestRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("RecRequest with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_ucsbdate_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(recRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/recommendationrequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recRequestRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("RecRequest with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_ucsbdate() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-02-04T00:00:00");

                RecRequest recRequestOrig = RecRequest.builder()
                                .requesterEmail("ryanhe@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("For BS/MS program")
                                .dateRequested(ldt1)
                                .dateNeeded(ldt1)
                                .done(true)
                                .build();

                RecRequest recRequestEdited = RecRequest.builder()
                                .requesterEmail("ryanhxhe@gmail.com")
                                .professorEmail("phtcon1@ucsb.edu")
                                .explanation("For PhD program")
                                .dateRequested(ldt2)
                                .dateNeeded(ldt2)
                                .done(false)
                                .build();

                String requestBody = mapper.writeValueAsString(recRequestEdited);

                when(recRequestRepository.findById(eq(67L))).thenReturn(Optional.of(recRequestOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/recommendationrequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recRequestRepository, times(1)).findById(67L);
                verify(recRequestRepository, times(1)).save(recRequestEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                RecRequest editedRecRequest = RecRequest.builder()
                                .requesterEmail("ryanhxhe@gmail.com")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("For PhD program")
                                .dateRequested(ldt1)
                                .dateNeeded(ldt1)
                                .done(false)
                                .build();

                String requestBody = mapper.writeValueAsString(editedRecRequest);

                when(recRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/recommendationrequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recRequestRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("RecRequest with id 67 not found", json.get("message"));

        }
}
