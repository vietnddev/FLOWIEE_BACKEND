package com.flowiee.app.security;

import com.flowiee.app.utils.EndPointUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	AccountDetailService accountDetailService;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(accountDetailService).passwordEncoder(passwordEncoder());
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> authenticationDetailsSource() {
		return new WebAuthenticationDetailsSource();
	}

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
//		httpSecurity.csrf().disable().cors();

		//Cấu hình phần này để có thể nhúng URL vào các thẻ như iframe,..
		//httpSecurity.headers().frameOptions().sameOrigin();

		httpSecurity
				.cors().and()
				.csrf().disable()
				.headers().frameOptions().sameOrigin().and() //Cấu hình phần này để có thể nhúng URL vào các thẻ như iframe,..
				.authorizeRequests()
				.antMatchers(EndPointUtil.SYS_CONFIG, EndPointUtil.SYS_ACCOUNT, EndPointUtil.SYS_LOG).hasRole("ADMIN")
				.antMatchers("/build/**", "/dist/**", "/js/**", "/plugins/**", "/uploads/**", "/actuator/**", "/swagger-ui/**").permitAll()
				.anyRequest().authenticated()
				.and()
				//Page login
				.formLogin().loginPage(EndPointUtil.SYS_LOGIN).permitAll()
				//Login OK thì redirect vào page danh sách sản phẩm
				.defaultSuccessUrl("/")
				.failureUrl(EndPointUtil.SYS_LOGIN + "?success=fail")
				.loginProcessingUrl("/j_spring_security_check")
				.authenticationDetailsSource(authenticationDetailsSource())
				.and()
				.httpBasic()
				.and()
				.logout()
				.logoutUrl(EndPointUtil.SYS_LOGOUT) // Endpoint cho đăng xuất
				.logoutSuccessUrl(EndPointUtil.SYS_LOGIN) // Đường dẫn sau khi đăng xuất thành công
				.deleteCookies("JSESSIONID") // Xóa cookies sau khi đăng xuất
				.invalidateHttpSession(true) // Hủy phiên làm việc
				.and()
				.userDetailsService(userDetailsService())
				//Page default if you are not authorized
				.exceptionHandling().accessDeniedPage("/error/403");
	}
//	@Bean
//	public WebMvcConfigurer corsConfigurer() {
//		return new WebMvcConfigurerAdapter() {
//			@Override
//			public void addCorsMappings(CorsRegistry registry) {
//				registry.addMapping("/**");
//			}
//		};
//	}

}