package com.company.ecommercebackend.api.security;

import com.company.ecommercebackend.model.LocalUser;
import com.company.ecommercebackend.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Map;
import java.util.regex.Matcher;

@EnableWebSocket
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private ApplicationContext context;
    private JWTRequestFilter jwtRequestFilter;
    private UserService userService;
    private static final AntPathMatcher MATCHER=new AntPathMatcher();

    public WebSocketConfiguration(ApplicationContext context, JWTRequestFilter jwtRequestFilter) {
        this.context = context;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket").setAllowedOriginPatterns("**").withSockJS();
    }

    @Override
    public void configureMessageBroker(org.springframework.messaging.simp.config.MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    private AuthorizationManager<Message<?>> makeMessageAuthorizationManager()
    {
        MessageMatcherDelegatingAuthorizationManager.Builder messages=
                new MessageMatcherDelegatingAuthorizationManager.Builder();
        messages.simpDestMatchers("/topic/user/**").authenticated()
                .simpTypeMatchers(SimpMessageType.MESSAGE).denyAll()
                .anyMessage().permitAll();
        return messages.build();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        AuthorizationManager<Message<?>> authorizationManager=makeMessageAuthorizationManager();
        AuthorizationChannelInterceptor interceptor=new AuthorizationChannelInterceptor(authorizationManager);
        AuthorizationEventPublisher eventPublisher=new SpringAuthorizationEventPublisher(context);
        interceptor.setAuthorizationEventPublisher(eventPublisher);
        registration.interceptors(jwtRequestFilter,interceptor,
                new RejectClientMessagesOnChannelInterceptor(),
                new DestinationLevelAuthorizationChannelInterceptor());
    }

    private String[] paths=new String[]
            {
                    "/topic/user/*/address"
            };

    private class RejectClientMessagesOnChannelInterceptor implements ChannelInterceptor
    {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            if(message.getHeaders().get("simpMessageType").equals(SimpMessageType.MESSAGE))
            {
                String destination = (String) message.getHeaders().get("simpDestionation");
                for(String path:paths)
                {
                    if(MATCHER.match(path,destination))
                    {
                        message=null;
                    }
                }
            }
            return message;
        }
    }

    private class DestinationLevelAuthorizationChannelInterceptor implements ChannelInterceptor
    {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            if(message.getHeaders().get("simpleMessageType").equals(SimpMessageType.SUBSCRIBE))
            {
                String destination = (String) message.getHeaders().get("simpDestionation");
                Map<String,String> params=MATCHER.extractUriTemplateVariables(
                        "/topic/user/{userId}/**",destination);
                try {
                    Long userId = Long.valueOf(params.get("userId"));
                    Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
                    if(authentication==null)
                    {
                        LocalUser user= (LocalUser) authentication.getPrincipal();
                        if(!userService.userHasPermisssionToUser(user,userId))
                        {
                            message=null;
                        }
                    }
                    else return message=null;
                }catch (NumberFormatException ex)
                {
                    message=null;
                }
            }
            return message;
        }
    }
}
