package com.example.healthcheckapi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class UserSecurityConfig extends WebSecurityConfigurerAdapter {

 @Autowired
 DataSource dataSource;

 @Bean
 public BCryptPasswordEncoder passwordEncoder() {
  System.out.println("in password encoder");
  return new BCryptPasswordEncoder();
 }

 // Enable jdbc authentication
 @Autowired
 public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
  auth.jdbcAuthentication().dataSource(dataSource).passwordEncoder(passwordEncoder());
 }

 @Override
 public void configure(WebSecurity web) throws Exception {
  web.ignoring().antMatchers("/v1/user/**").antMatchers("/healthz");
 }
}
