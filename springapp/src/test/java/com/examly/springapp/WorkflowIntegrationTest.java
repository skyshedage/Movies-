package com.examly.springapp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc
class WorkflowIntegrationTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper json;
 @Autowired com.examly.springapp.repository.UserRepo users;
 String register(String who)throws Exception {
  mvc.perform(post("/api/user/register").contentType("application/json").content("{\"username\":\""+who+"\",\"email\":\""+who+"@example.invalid\",\"password\":\"LocalTest!123\",\"userRole\":\"ADMIN\"}")).andExpect(status().isCreated()).andExpect(jsonPath("$.userRole").value("USER"));return login(who);
 }
 String login(String who)throws Exception {
  String s=mvc.perform(post("/api/user/login").contentType("application/json").content("{\"email\":\""+who+"@example.invalid\",\"password\":\"LocalTest!123\"}")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();return "Bearer "+json.readTree(s).get("jwtToken").asText();
 }
 @Test void adminMovieAndUserBookingLifecycle()throws Exception {
  String user=register("movieuser"),other=register("movieother");register("movieadmin");var a=users.findByEmail("movieadmin@example.invalid");a.setUserRole("ADMIN");users.save(a);String admin=login("movieadmin");int uid=users.findByEmail("movieuser@example.invalid").getUserId();
  String s=mvc.perform(post("/api/movie").header("Authorization",admin).contentType("application/json").content("{\"id\":99999,\"title\":\"Test feature\",\"duration\":90,\"genre\":\"Drama\",\"price\":10,\"totalSeats\":3}")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();long mid=json.readTree(s).get("id").asLong();org.junit.jupiter.api.Assertions.assertNotEquals(99999,mid);
  s=mvc.perform(post("/api/booking/"+uid+"/"+mid).header("Authorization",user).contentType("application/json").content("{\"seatCount\":2,\"totalCost\":0}")).andExpect(status().isCreated()).andExpect(jsonPath("$.totalCost").value(20)).andReturn().getResponse().getContentAsString();long bid=json.readTree(s).get("id").asLong();
  mvc.perform(get("/api/booking/user/"+uid).header("Authorization",user)).andExpect(status().isOk());
  mvc.perform(get("/api/booking").header("Authorization",admin)).andExpect(status().isOk());
  mvc.perform(delete("/api/booking/"+bid).header("Authorization",other)).andExpect(status().isForbidden());
  mvc.perform(put("/api/movie/"+mid).header("Authorization",admin).contentType("application/json").content("{\"title\":\"Test\",\"duration\":90,\"price\":10,\"totalSeats\":1}")).andExpect(status().isBadRequest());
  mvc.perform(delete("/api/booking/"+bid).header("Authorization",user)).andExpect(status().isOk());
  mvc.perform(delete("/api/movie/"+mid).header("Authorization",admin)).andExpect(status().isOk());
 }
}
