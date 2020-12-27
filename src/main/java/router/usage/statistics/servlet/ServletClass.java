package router.usage.statistics.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import router.usage.statistics.model.ModelClass;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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

        String selected = request.getParameter("selected");
        if (selected == null || selected.isEmpty()) {
            selected = now().getYear() + "-" + now().getMonthValue();
        }

        String[] selectedYearMonth = selected.split("-");
        List<String> selectedYear = singletonList(selectedYearMonth[0]);
        List<String> selectedMonth = singletonList(selectedYearMonth[1]);

        Set<String> yearMonthSet = retrieveUniqueDatesOnly();
        List<ModelClass> modelClassList = retrieveDataUsages(selectedYear, selectedMonth);
        ModelClass modelClassTotal = calculateTotalDataUsage(modelClassList);

        String htmlToDisplay = getDisplay(modelClassList, modelClassTotal, selected, yearMonthSet);
        response.getWriter().println(htmlToDisplay);
    }
}
