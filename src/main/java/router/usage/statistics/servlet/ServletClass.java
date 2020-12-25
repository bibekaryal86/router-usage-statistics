package router.usage.statistics.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import router.usage.statistics.model.ModelClass;

import java.io.IOException;
import java.util.List;

import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static router.usage.statistics.service.ServiceClass.*;
import static router.usage.statistics.view.ViewClass.getDisplay;

public class ServletClass extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        List<ModelClass> modelClassList = retrieveDataUsages(singletonList(String.valueOf(now().getYear())),
                singletonList(String.valueOf(now().getMonthValue())));
        ModelClass modelClassTotal = calculateTotalDataUsage(modelClassList);
        ModelClass modelClassToday = getWanTrafficTodayOnly();
        String htmlToDisplay = getDisplay(modelClassList, modelClassTotal, modelClassToday);

        response.getWriter().println(htmlToDisplay);
    }
}
