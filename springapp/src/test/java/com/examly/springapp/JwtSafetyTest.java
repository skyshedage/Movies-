package com.examly.springapp;
import com.examly.springapp.configuration.JwtUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class JwtSafetyTest {
 @Test void rejectsMissingOrShortSecret(){
   assertThrows(IllegalArgumentException.class,()->new JwtUtils(""));
   assertThrows(IllegalArgumentException.class,()->new JwtUtils("short"));
 }
 @Test void issuesNewDayLongTokenForEachLogin(){
   JwtUtils jwt=new JwtUtils("test-only-secret-at-least-32-bytes-long");
   long before=System.currentTimeMillis();String token=jwt.generateToken("test@example.invalid");
   assertEquals("test@example.invalid",jwt.extractUsername(token));
   long ttl=jwt.extractExpiration(token).getTime()-before;
   assertTrue(ttl>86_398_000 && ttl<=86_400_000);
 }
}
