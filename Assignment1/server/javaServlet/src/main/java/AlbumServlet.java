import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.servlet.http.Part;

@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 50,    // 50 MB
        maxFileSize = 1024 * 1024 * 50,        // 50 MB
        maxRequestSize = 1024 * 1024 * 100)    // 100 MB
@WebServlet(value = "/albums/*")
public class AlbumServlet extends HttpServlet {

    private Gson gson = new Gson();
    private static final String UPLOAD_DIR = "uploads";

    // Simulate retrieving album information from a data source
    private String getAlbumInfo(String albumID) {
        // Replace this with your actual logic to fetch album info
        // Return null if album with the given key is not found
        // Otherwise, return JSON representation of the album info
        AlbumInfo albumExample = new AlbumInfo("Yanlin", "best", "2023");
        String albumInfo = this.gson.toJson(albumExample);
        return albumInfo;
    }

    private String postAlbum() {
        // Replace this with your actual logic to fetch album info
        // Return null if album with the given key is not found
        // Otherwise, return JSON representation of the album info
        ImageMetaData imageExample = new ImageMetaData("javaAlbumID", "normal");
        String imageInfo = this.gson.toJson(imageExample);
        return imageInfo;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Handle GET requests here

        // Retrieve the 'albumID' path parameter
        String albumID = request.getPathInfo();


        // Assuming 'albumInfo' is a JSON representation of album information
        String albumInfo = getAlbumInfo(albumID);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        if (albumInfo != null) {
            // If album information is found, send a 200 OK response with JSON data
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(albumInfo);
            out.flush();
        } else {
            // If album information is not found, send a 404 response
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"Error in doGet\": \"Key not found\"}");
            out.flush();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            // Get the 'image' part from the request
            Part imagePart = request.getPart("image");
            Part profilePart = request.getPart("profile");
            if (imagePart == null || profilePart == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("Error: imagePart or profilePart is null");
                out.flush();
            }

            //Get InputString from both parts
            //InputStream imageInputStream = imagePart.getInputStream();
            InputStream profileInputStream = profilePart.getInputStream();

            // gets absolute path of the web application
            String applicationPath = request.getServletContext().getRealPath("");
            // constructs path of the directory to save uploaded file
            String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;
            request.setAttribute("message", uploadFilePath + " uploadFilePath!");
            // creates the save directory if it does not exists
            File fileSaveDir = new File(uploadFilePath);
            if (!fileSaveDir.exists()) {
                fileSaveDir.mkdirs();
            }
            // Read 'image' binary data and save it to a file (you may change the file path as needed)

            String fileName = getFileName(imagePart);
            System.out.println("filename: " + fileName);
            imagePart.write(uploadFilePath + File.separator + fileName);
            request.setAttribute("message", fileName + " File uploaded successfully!");

            // TODO: Parse profile object
            // Use objectMapper to read and parse the JSON from the InputStream
            // ObjectMapper objectMapper = new ObjectMapper();
            // JsonNode jsonNode = objectMapper.readTree(profileInputStream);

            // Handle the received data as needed
            String imageInfo = postAlbum();

            // Respond with a success message
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(imageInfo);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("Error in doPost: " + e.getMessage());
            out.flush();
        }
    }

    /**
     * Utility method to get file name from HTTP header content-disposition
     */
    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        System.out.println("content-disposition header= " + contentDisp);
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }
}
