import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class AppLauncher extends HttpServlet {
    // static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Weather App Launcher</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background-color: #f0f0f0; }");
        out.println(".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; border: 1px solid #ccc; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        out.println("h1, h2 { text-align: center; }");
        out.println("a { color: #007bff; text-decoration: none; }");
        out.println("a:hover { text-decoration: underline; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class=\"container\">");
        out.println("<h1>Welcome to Weather App!</h1>");
        out.println("<h2>Click <a href=\"WeatherAppGui\">here</a> to launch the Weather App.</h2>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

   
}
