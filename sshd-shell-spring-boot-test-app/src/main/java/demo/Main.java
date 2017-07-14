package demo;

import java.util.Collection;
import java.util.HashSet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author anand
 */
@SpringBootApplication
public class Main {

    @Bean
    public AuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(username -> new UserDetails() {
            private static final long serialVersionUID = 1L;

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                Collection<GrantedAuthority> authorities = new HashSet<>();
                authorities.add(new SimpleGrantedAuthority("USER"));
                if (username.equals("admin")) {
                    authorities.add(new SimpleGrantedAuthority("ADMIN"));
                }
                return authorities;
            }

            @Override
            public String getPassword() {
                return username;
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        });
        return authProvider;
    }

    public static void main(String... args) throws InterruptedException {
        SpringApplication.run(Main.class, args);
        Thread.sleep(Long.MAX_VALUE);
    }
}
