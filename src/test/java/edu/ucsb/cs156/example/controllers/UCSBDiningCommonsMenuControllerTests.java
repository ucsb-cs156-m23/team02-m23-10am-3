package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenu;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;

@WebMvcTest(controllers = UCSBDiningCommonsMenuController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuControllerTests extends ControllerTestCase {

        @MockBean
        UCSBDiningCommonsMenuRepository ucsbDiningCommonsMenuRepository;

        @MockBean
        UserRepository userRepository;
        //Authorization tests for /api/ucsbdiningcommonsmenu/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenu/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenu/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenu?id=1"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/ucsbdiningcommonsmenu/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommonsmenu/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommonsmenu/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBDiningCommonsMenu ucsbDiningCommonsMenu = UCSBDiningCommonsMenu.builder()
                                .name("Baked Pesto Pasta with Chicken")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();

                when(ucsbDiningCommonsMenuRepository.findById(eq(1L))).thenReturn(Optional.of(ucsbDiningCommonsMenu));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenu?id=1"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuRepository, times(1)).findById(eq(1L));
                String expectedJson = mapper.writeValueAsString(ucsbDiningCommonsMenu);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbDiningCommonsMenuRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenu?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBDiningCommonsMenu with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbdates() throws Exception {

                // arrange

                UCSBDiningCommonsMenu ucsbDiningCommonsMenu1 = UCSBDiningCommonsMenu.builder()
                                .diningCommonsCode("ortega")
                                .name("Baked%20Pesto%20Pasta%20with%20Chicken")
                                .station("Entree%20Specials")
                                .build();


                UCSBDiningCommonsMenu ucsbDiningCommonsMenu2 = UCSBDiningCommonsMenu.builder()
                                .diningCommonsCode("portola")
                                .name("Cream%20of%20Broccoli%20Soup%20(v)")
                                .station("Greens%20&%20Grains")
                                .build();

                ArrayList<UCSBDiningCommonsMenu> expectedUCSBDiningCommonsMenus = new ArrayList<>();
                expectedUCSBDiningCommonsMenus.addAll(Arrays.asList(ucsbDiningCommonsMenu1, ucsbDiningCommonsMenu2));

                when(ucsbDiningCommonsMenuRepository.findAll()).thenReturn(expectedUCSBDiningCommonsMenus);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenu/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedUCSBDiningCommonsMenus);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_ucsbdiningcommonsmenu() throws Exception {
                // arrange

                UCSBDiningCommonsMenu ucsbDiningCommonsMenu1 = UCSBDiningCommonsMenu.builder()
                                .diningCommonsCode("ortega")
                                .name("Baked%20Pesto%20Pasta%20with%20Chicken")
                                .station("Entree%20Specials")
                                .build();

                when(ucsbDiningCommonsMenuRepository.save(eq(ucsbDiningCommonsMenu1))).thenReturn(ucsbDiningCommonsMenu1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsbdiningcommonsmenu/post?id=0&diningCommonsCode=ortega&name=Baked%20Pesto%20Pasta%20with%20Chicken&station=Entree%20Specials")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuRepository, times(1)).save(ucsbDiningCommonsMenu1);
                String expectedJson = mapper.writeValueAsString(ucsbDiningCommonsMenu1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_diningcommonsmenu() throws Exception {
                // arrange

                UCSBDiningCommonsMenu ucsbDiningCommonsMenu1 = UCSBDiningCommonsMenu.builder()
                                .diningCommonsCode("ortega")
                                .name("Baked%20Pesto%20Pasta%20with%20Chicken")
                                .station("Entree%20Specials")
                                .build();

                when(ucsbDiningCommonsMenuRepository.findById(eq(1L))).thenReturn(Optional.of(ucsbDiningCommonsMenu1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbdiningcommonsmenu?id=1")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuRepository, times(1)).findById(1L);
                verify(ucsbDiningCommonsMenuRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenu with id 1 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_ucsbdate_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbDiningCommonsMenuRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbdiningcommonsmenu?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenu with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_ucsbdiningcommonsmenu() throws Exception {
                // arrange

                UCSBDiningCommonsMenu ucsbDiningCommonsMenuOrig = UCSBDiningCommonsMenu.builder()
                                .diningCommonsCode("ortega")
                                .name("Baked%20Pesto%20Pasta%20with%20Chicken")
                                .station("Entree%20Specials")
                                .build();

                UCSBDiningCommonsMenu ucsbDiningCommonsMenuEdited = UCSBDiningCommonsMenu.builder()
                                .diningCommonsCode("portola")
                                .name("Cream%20of%20Broccoli%20Soup%20(v)")
                                .station("Greens%20&%20Grains")
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbDiningCommonsMenuEdited);

                when(ucsbDiningCommonsMenuRepository.findById(eq(67L))).thenReturn(Optional.of(ucsbDiningCommonsMenuOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdiningcommonsmenu?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuRepository, times(1)).findById(67L);
                verify(ucsbDiningCommonsMenuRepository, times(1)).save(ucsbDiningCommonsMenuEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_ucsbdiningcommonsmenu_that_does_not_exist() throws Exception {
                // arrange

                UCSBDiningCommonsMenu ucsbEditedDiningCommonsMenu = UCSBDiningCommonsMenu.builder()
                                .diningCommonsCode("ortega")
                                .name("Baked%20Pesto%20Pasta%20with%20Chicken")
                                .station("Entree%20Specials")
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbEditedDiningCommonsMenu);

                when(ucsbDiningCommonsMenuRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdiningcommonsmenu?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenu with id 67 not found", json.get("message"));

        }
}
