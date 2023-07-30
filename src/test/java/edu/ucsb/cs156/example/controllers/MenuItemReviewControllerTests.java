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

        @Test
        public void logged_out_users_cannot_get_index() throws Exception {
            mockMvc.perform(get("/api/menuitemreview/all"))
                .andExpect(status().is(403)); // Forbidden
        }


        @Test
        public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/menuitemreview/post"))
                .andExpect(status().is(403));
        }

        @WithMockUser(roles = {"USER"})
        @Test
        public void logged_in_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/menuitemreview/post"))
                .andExpect(status().is(403));
        }

        @WithMockUser(roles = {"USER"})
        @Test
        public void logged_in_users_can_get_index() throws Exception {
            mockMvc.perform(get("/api/menuitemreview/all"))
                .andExpect(status().is(200)); // Ok
        }

        @WithMockUser(roles = {"USER"})
        @Test
        public void logged_in_users_can_get_all_menuitemreviews() throws Exception {
            
            // arrange
            MenuItemReview first = MenuItemReview.builder()
                .itemId(1L)
                .reviewerEmail("a@ucsb.edu")
                .stars(1)
                .dateReviewed(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                .comments("First")
                .build();

            MenuItemReview second = MenuItemReview.builder()
                .itemId(2L)
                .reviewerEmail("b@ucsb.edu")
                .stars(2)
                .dateReviewed(LocalDateTime.of(2023, 1, 2, 0, 0, 0))
                .comments("Second")
                .build();

            MenuItemReview third = MenuItemReview.builder()
                .itemId(3L)
                .reviewerEmail("c@ucsb.edu")
                .stars(3)
                .dateReviewed(LocalDateTime.of(2023, 1, 3, 0, 0, 0))
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
        public void admin_user_can_post() throws Exception {
            // arrange
            MenuItemReview first = MenuItemReview.builder()
                .itemId(1L)
                .reviewerEmail("a@ucsb.edu")
                .stars(1)
                .dateReviewed(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                .comments("First")
                .build();
            
            when(menuItemReviewRepository.save(eq(first))).thenReturn(first);
        
            // act
            MvcResult response = mockMvc.perform(
                post("/api/menuitemreview/post?itemId=1&reviewerEmail=a@ucsb.edu&stars=1&dateReviewed=2023-01-01T00:00:00&comments=First")
                    .with(csrf()))
                .andExpect(status().is(200)).andReturn();

            // assert
            verify(menuItemReviewRepository, times(1)).save(first);
            String expectedJson = mapper.writeValueAsString(first);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
        }

}