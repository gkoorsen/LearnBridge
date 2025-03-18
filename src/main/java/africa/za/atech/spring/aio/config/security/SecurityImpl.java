package africa.za.atech.spring.aio.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityImpl {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(auth -> auth

                .requestMatchers("/css/*.css").permitAll()
                .requestMatchers("/js/*.js").permitAll()
                .requestMatchers("/images/*.png").permitAll()

                .requestMatchers("/error").permitAll()
                .requestMatchers("/login").permitAll()

                .requestMatchers("/register/**").permitAll()
                .requestMatchers("/forgot").permitAll()

                .requestMatchers("/admin/admin/organisation/**").hasAnyRole("ORG_ADMIN", "ADMIN")
                .requestMatchers("/admin/admin/module/**").hasAnyRole("ORG_ADMIN", "ADMIN")
                .requestMatchers("/admin/admin/users/**").hasAnyRole("MANAGER", "ORG_ADMIN", "ADMIN")

                .requestMatchers("/admin/admin/assistants/**").hasAnyRole("MANAGER", "ORG_ADMIN", "ADMIN")
                .requestMatchers("/chat/**").hasAnyRole("USER", "MANAGER", "ORG_ADMIN", "ADMIN")

                .requestMatchers("/profile/**").hasAnyRole("USER", "MANAGER", "ORG_ADMIN", "ADMIN")

                .anyRequest().authenticated()
        );

        http.formLogin(auth -> auth
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .permitAll());

        http.logout(auth -> auth
                .deleteCookies("JSESSIONID")
                .logoutUrl("/logout")
        );
        return http.build();
    }
}
