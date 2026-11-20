package com.examly.springapp.configuration;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
 @Autowired private JwtUtils utils;
 @Autowired private UserDetailsService userService;
 @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
   throws ServletException, IOException {
   String header=request.getHeader("Authorization");
   if(header!=null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication()==null){
     try {
       String token=header.substring(7);
       String username=utils.extractUsername(token);
       UserDetails user=userService.loadUserByUsername(username);
       if(utils.validateToken(token,user)){
         var auth=new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
         auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
         SecurityContextHolder.getContext().setAuthentication(auth);
       }
     } catch(io.jsonwebtoken.JwtException | IllegalArgumentException | org.springframework.security.core.userdetails.UsernameNotFoundException e) {
       SecurityContextHolder.clearContext();
       response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Invalid authentication token");
       return;
     }
   }
   chain.doFilter(request,response);
 }
}
