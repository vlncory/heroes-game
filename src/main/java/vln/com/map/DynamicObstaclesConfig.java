package vln.com.map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DynamicObstaclesConfig {
    public record ObstacleData(int startX, int startY, List<DynamicObstacle.Point> path) {}

    public static void saveToXML(String mapName, List<ObstacleData> obstacles) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("dynamicObstacles");
        doc.appendChild(root);

        for (ObstacleData obstacle : obstacles) {
            Element obstacleEl = doc.createElement("obstacle");
            Element startEl = doc.createElement("start");
            startEl.setAttribute("x", String.valueOf(obstacle.startX()));
            startEl.setAttribute("y", String.valueOf(obstacle.startY()));
            obstacleEl.appendChild(startEl);

            Element pathEl = doc.createElement("path");
            for (DynamicObstacle.Point point : obstacle.path()) {
                Element pointEl = doc.createElement("point");
                pointEl.setAttribute("x", String.valueOf(point.x()));
                pointEl.setAttribute("y", String.valueOf(point.y()));
                pathEl.appendChild(pointEl);
            }
            obstacleEl.appendChild(pathEl);
            root.appendChild(obstacleEl);
        }

        // Resolve the base directory relative to user.dir
        String basePath = System.getProperty("user.dir");
        File dir = new File(basePath, "src/main/resources/maps");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new Exception("Failed to create maps directory: " + dir.getAbsolutePath());
        }

        File outputFile = new File(dir, mapName + "_dynamic.xml");
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(outputFile));
    }

    public static List<ObstacleData> loadFromXML(String mapName) throws Exception {
        List<ObstacleData> obstacles = new ArrayList<>();
        String basePath = System.getProperty("user.dir");
        File file = new File(basePath, "src/main/resources/maps/" + mapName + "_dynamic.xml");
        if (!file.exists()) {
            return obstacles;
        }

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        NodeList obstacleNodes = doc.getElementsByTagName("obstacle");

        for (int i = 0; i < obstacleNodes.getLength(); i++) {
            Element obstacleEl = (Element) obstacleNodes.item(i);
            Element startEl = (Element) obstacleEl.getElementsByTagName("start").item(0);
            int startX = Integer.parseInt(startEl.getAttribute("x"));
            int startY = Integer.parseInt(startEl.getAttribute("y"));

            List<DynamicObstacle.Point> path = new ArrayList<>();
            NodeList pointNodes = obstacleEl.getElementsByTagName("point");
            for (int j = 0; j < pointNodes.getLength(); j++) {
                Element pointEl = (Element) pointNodes.item(j);
                int x = Integer.parseInt(pointEl.getAttribute("x"));
                int y = Integer.parseInt(pointEl.getAttribute("y"));
                path.add(new DynamicObstacle.Point(x, y));
            }

            obstacles.add(new ObstacleData(startX, startY, path));
        }
        return obstacles;
    }
}