package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;

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

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase { 
    
        @MockBean
        MenuItemReviewRepository menuItemReviewRepository;
    
        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/menuitemreview/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/menuitemreview/all"))
                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = {"USER"})
        @Test
        public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/menuitemreview/all"))
                .andExpect(status().is(200)); // logged in users can get all
        }

        @Test
        public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/menuitemreview/post"))
                .andExpect(status().is(403)); 
        }

        @WithMockUser(roles = {"USER"})
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/menuitemreview/post"))
                .andExpect(status().is(403));
        }

        @WithMockUser(roles = {"USER"})
        @Test
        public void logged_in_user_can_get_all_menu_item_reviews() throws Exception {
            // arrange
            MenuItemReview first = MenuItemReview.builder()
                .itemId(1L)
                .reviewerEmail("andrewyu@ucsb.edu")
                .stars(1)
                .dateReviewed(LocalDateTime.of(2023, 7, 29, 0, 0, 0))
                .comments("First")
                .build();

            MenuItemReview second = MenuItemReview.builder()
                .itemId(2L)
                .reviewerEmail("andrewyu@ucsb.edu")
                .stars(2)
                .dateReviewed(LocalDateTime.of(2023, 7, 29, 6, 0, 0))
                .comments("Second")
                .build();

            MenuItemReview third = MenuItemReview.builder()
                .itemId(3L)
                .reviewerEmail("andrewyu@ucsb.edu")
                .stars(3)
                .dateReviewed(LocalDateTime.of(2023, 7, 29, 12, 0, 0))
                .comments("Third")
                .build();

                ArrayList<MenuItemReview> allReviews = new ArrayList<MenuItemReview>();
                allReviews.addAll(Arrays.asList(first, second, third));

            when(menuItemReviewRepository.findAll()).thenReturn(allReviews);

            // act
            MvcResult response = mockMvc.perform(get("/api/menuitemreview/all"))
                .andExpect(status().is(200)).andReturn();

            // assert
            verify(menuItemReviewRepository, times(1)).findAll();
            String expectedJson = mapper.writeValueAsString(allReviews);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = {"ADMIN", "USER"})
        @Test
        public void an_admin_user_can_post() throws Exception {
            // arrange
            MenuItemReview adminReview = MenuItemReview.builder()
                .itemId(1L)
                .reviewerEmail("admin@ucsb.edu")
                .stars(5)
                .dateReviewed(LocalDateTime.of(2023, 7, 29, 0, 0, 0))
                .comments("Admin")
                .build();
            
            when(menuItemReviewRepository.save(eq(adminReview))).thenReturn(adminReview);
        
            // act
            MvcResult response = mockMvc.perform(
                post("/api/menuitemreview/post?itemId=1&reviewerEmail=admin@ucsb.edu&stars=5&dateReviewed=2023-07-29T00:00:00&comments=Admin")
                    .with(csrf()))
                .andExpect(status().is(200)).andReturn();

            // assert
            verify(menuItemReviewRepository, times(1)).save(adminReview);
            String expectedJson = mapper.writeValueAsString(adminReview);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/menuitemreview?id=1"))
                .andExpect(status().isForbidden());
        }

        @WithMockUser(roles = {"USER"})
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
            // arrange
            MenuItemReview menuItemReview = MenuItemReview.builder()
                .itemId(1)
                .reviewerEmail("andrewyu@ucsb.edu")
                .stars(5)
                .dateReviewed(LocalDateTime.of(2023, 7, 29, 0, 0, 0))
                .comments("Review")
                .build();

            when(menuItemReviewRepository.findById(1L)).thenReturn(Optional.of(menuItemReview));

            // act
            MvcResult response = mockMvc.perform(get("/api/menuitemreview?id=1"))
                .andExpect(status().is(200)).andReturn();

            // assert

            verify(menuItemReviewRepository, times(1)).findById(1L);
            String expectedJson = mapper.writeValueAsString(menuItemReview);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = {"USER"})
        @Test
        public void test_that_logged_in_user_cannot_get_by_id_when_the_id_does_not_exist() throws Exception {
            // arrange
            when(menuItemReviewRepository.findById(1L)).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(get("/api/menuitemreview?id=1"))
                .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(menuItemReviewRepository, times(1)).findById(1L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("EntityNotFoundException", json.get("type"));
            assertEquals("MenuItemReview with id 1 not found", json.get("message"));
        }

        @WithMockUser(roles = {"ADMIN", "USER"})
        @Test
        public void admin_can_edit_an_existing_menuitemreview() throws Exception {
            // arrange
            MenuItemReview first = MenuItemReview.builder()
                .itemId(1L)
                .reviewerEmail("andrewyu@ucsb.edu")
                .stars(1)
                .dateReviewed(LocalDateTime.of(2023, 7, 29, 0, 0, 0))
                .comments("First")
                .build();

            MenuItemReview second = MenuItemReview.builder()
                .itemId(2L)
                .reviewerEmail("andrewyu@ucsb.edu")
                .stars(2)
                .dateReviewed(LocalDateTime.of(2023, 7, 29, 6, 0, 0))
                .comments("Second")
                .build();

            String requestBody = mapper.writeValueAsString(second);

            when(menuItemReviewRepository.findById(eq(1L))).thenReturn(Optional.of(first));

            // act
            MvcResult response = mockMvc.perform(
                put("/api/menuitemreview?id=1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
                .andExpect(status().isOk()).andReturn();

            // assert
            verify(menuItemReviewRepository, times(1)).findById(1L);
            verify(menuItemReviewRepository, times(1)).save(second);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = {"ADMIN", "USER"})
        @Test
        public void admin_cannot_edit_menuitemreview_that_does_not_exist() throws Exception {
            // arrange
            MenuItemReview menuItemReview = MenuItemReview.builder()
                .itemId(20L)
                .reviewerEmail("andrewyu@ucsb.edu")
                .stars(1)
                .dateReviewed(LocalDateTime.of(2023, 7, 29, 0, 0, 0))
                .comments("First")
                .build();

            String requestBody = mapper.writeValueAsString(menuItemReview);
            when(menuItemReviewRepository.findById(eq(1L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                put("/api/menuitemreview?id=1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(menuItemReviewRepository, times(1)).findById(1L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("MenuItemReview with id 1 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete() throws Exception {
            // arrange
            MenuItemReview menuItemReview = MenuItemReview.builder()
                .itemId(1L)
                .reviewerEmail("andrewyu@ucsb.edu")
                .stars(1)
                .dateReviewed(LocalDateTime.of(2023, 7, 29, 0, 0, 0))
                .comments("First")
                .build();

            when(menuItemReviewRepository.findById(eq(1L))).thenReturn(Optional.of(menuItemReview));

            // act
            MvcResult response = mockMvc.perform(
                delete("/api/menuitemreview?id=1")
                    .with(csrf()))
                .andExpect(status().isOk()).andReturn();

            // assert
            verify(menuItemReviewRepository, times(1)).findById(1L);
            verify(menuItemReviewRepository, times(1)).delete(any());

            Map<String, Object> json = responseToJson(response);
            assertEquals("MenuItemReview with id 1 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_menuitemreview() throws Exception {
            // arrange
            when(menuItemReviewRepository.findById(eq(1L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                delete("/api/menuitemreview?id=1")
                    .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(menuItemReviewRepository, times(1)).findById(1L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("MenuItemReview with id 1 not found", json.get("message"));
        }
}