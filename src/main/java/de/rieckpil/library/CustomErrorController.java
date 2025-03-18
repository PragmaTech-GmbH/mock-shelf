package de.rieckpil.library;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

  private static final Logger LOG = LoggerFactory.getLogger(CustomErrorController.class);

  @RequestMapping("/error")
  public String handleError(HttpServletRequest request, Model model) {
    // Get error details
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
    Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

    if (exception != null) {
      LOG.error("Error: {}", exception);
    }

    if (status != null) {
      Integer statusCode = Integer.valueOf(status.toString());

      model.addAttribute("statusCode", statusCode);
      model.addAttribute("errorMessage", message);

      if (statusCode == 404) {
        return "error/404";
      } else if (statusCode == 403) {
        return "error/403";
      } else if (statusCode >= 500) {
        return "error/500";
      }
    }

    return "error/general";
  }
}
