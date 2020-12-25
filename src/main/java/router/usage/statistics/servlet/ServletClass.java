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
        String defaultSelected = now().getYear() + "-" + now().getMonthValue();
        boolean isTotalTodayNeeded = false;

        if (selected == null || selected.isEmpty()) {
            selected = now().getYear() + "-" + now().getMonthValue();
        }

        if (selected.equals(defaultSelected)) {
            isTotalTodayNeeded = true;
        }

        String[] selectedYearMonth = selected.split("-");
        List<String> selectedYear = singletonList(selectedYearMonth[0]);
        List<String> selectedMonth = singletonList(selectedYearMonth[1]);

        Set<String> yearMonthSet = retrieveUniqueDatesOnly();
        List<ModelClass> modelClassList = retrieveDataUsages(selectedYear, selectedMonth);

        ModelClass modelClassTotal = calculateTotalDataUsage(modelClassList);
        ModelClass modelClassToday = newModelClass();

        if (isTotalTodayNeeded) {
            modelClassToday = getWanTraffic("all", "hour", "24").get(0);
        }

        String htmlToDisplay = getDisplay(modelClassList, modelClassTotal, modelClassToday, selected, yearMonthSet, isTotalTodayNeeded);
        response.getWriter().println(htmlToDisplay);
    }

    private ModelClass newModelClass() {
        return new ModelClass(null, null, null, null, "0.00", "0.00", "0.00");
    }
}
