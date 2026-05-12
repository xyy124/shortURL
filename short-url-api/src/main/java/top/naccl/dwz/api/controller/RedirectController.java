package top.naccl.dwz.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import top.naccl.dwz.core.service.UrlMappingService;
import top.naccl.dwz.core.visit.VisitLogger;

@Controller
public class RedirectController {
    @Autowired
    private UrlMappingService urlMappingService;
    @Autowired
    private VisitLogger visitLogger;

    @GetMapping("/{shortCode}")
    public String redirect(@PathVariable String shortCode, HttpServletRequest request) {
        String longUrl = urlMappingService.getLongUrlByShortCode(shortCode);
        if (longUrl != null) {
            urlMappingService.updateViews(shortCode);
            visitLogger.logVisit(shortCode, request);
            return "redirect:" + longUrl;
        }
        return "redirect:/";
    }
}
