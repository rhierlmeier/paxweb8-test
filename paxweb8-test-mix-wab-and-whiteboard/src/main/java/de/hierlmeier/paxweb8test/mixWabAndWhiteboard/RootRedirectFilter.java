package de.hierlmeier.paxweb8test.mixWabAndWhiteboard;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@Component(
        property = {HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN + "=/*"}
)
public class RootRedirectFilter implements Filter {

    private final Logger m_Logger = Logger.getLogger(RootRedirectFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if(servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            m_Logger.info("doFilter " + httpRequest.getRequestURI());
            if("/".equals(httpRequest.getRequestURI())) {
                m_Logger.info("Redirecting to WAB");
                HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
                httpServletResponse.sendRedirect("/wab");
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
