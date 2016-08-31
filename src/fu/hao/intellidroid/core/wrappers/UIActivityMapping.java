package fu.hao.intellidroid.core.wrappers;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import fu.hao.intellidroid.utils.Log;
import fu.hao.intellidroid.utils.Settings;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 * Description: Identify UI elements from XMLs
 * Authors: Hao Fu(haofu@ucdavis.edu)
 * Date: 2016/8/30
 */
public class UIActivityMapping {
    private final String TAG = this.getClass().getSimpleName();
    private final IClassHierarchy classHierarchy;
    private Map<String, Set<String>> handlerLayoutMap = new HashMap<>();
    private Map<String, Set<TypeReference>> handlerActivityMap = new HashMap<>();


    public UIActivityMapping(IClassHierarchy classHierarchy) {
        Log.debug(TAG, "=================================================");

        this.classHierarchy = classHierarchy;
        getLayoutIDs();
        try {
            getLayoutHandlers();
        } catch (Exception e) {
            Log.warn(TAG, e.getMessage());
        }
    }

    private Map<Integer, String> getLayoutIDs() {
        Map<Integer, String> idLayoutMap = new HashMap<>();
        try {
            File resourceFile = new File(Settings.getAppDirectory() + "/apk/res/values/public.xml");
            if (!resourceFile.exists()) {
                Log.warn(TAG, "Resource file not found!");
                return null;
            }

            // Parsing the xml
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document resourceXML = documentBuilder.parse(resourceFile);
            resourceXML.getDocumentElement().normalize();

            //NodeList publicNodes = resourceXML.getElementsByTagName("public");
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/resources//public[@type=\"layout\"]");
            NodeList layoutNodes = (NodeList) expr.evaluate(resourceXML, XPathConstants.NODESET);

            if (layoutNodes != null) {
                for (int i = 0; i < layoutNodes.getLength(); i++) {
                    Node layoutNode = layoutNodes.item(i);
                    if (layoutNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    Element layoutElement = (Element) layoutNode;
                    Integer layoutID = Integer.decode(layoutElement.getAttribute("id"));
                    String layoutName = layoutElement.getAttribute("name");

                    Log.debug(TAG, "Layout: " + layoutName + "; id: " + layoutID);

                    idLayoutMap.put(layoutID, layoutName);
                }
            }
        } catch (Exception e) {
            Log.warn(TAG, e.getMessage());
        }

        return idLayoutMap;
    }

    private void getLayoutHandlers() throws Exception {
        File resourceDir = new File(Settings.getAppDirectory() + "/apk/res/");
        if (!resourceDir.exists() || !resourceDir.isDirectory()) {
            Log.warn(TAG, "Cannot find the res file!");
            return;
        }

        File[] layoutDirs = resourceDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.startsWith("layout")) {
                    return true;
                }

                return false;
            }
        });

        for (File layoutDir : layoutDirs) {
            File[] xmlFiles = layoutDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.endsWith(".xml")) {
                        return true;
                    }

                    return false;
                }
            });

            for (File layoutXmlFile : xmlFiles) {
                processLayoutXml(layoutXmlFile);
            }
        }
    }

    private void processLayoutXml(File xmlFile) throws Exception {
        String layoutName = FilenameUtils.removeExtension(xmlFile.getName());

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document resourceXML = dBuilder.parse(xmlFile);
        resourceXML.getDocumentElement().normalize();

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//@*[local-name()='id']/..");
        NodeList uiElementNodes = (NodeList) expr.evaluate(resourceXML, XPathConstants.NODESET);

        // Collection<IClass> activitySubclasses = classHierarchy.computeSubClasses(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/app/Activity"));

        for (int i = 0; i < uiElementNodes.getLength(); i++) {
            Node uiElementNode = uiElementNodes.item(i);
            if (uiElementNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element uiElement = (Element) uiElementNode;

            // String elementName = uiElement.getTagName();
            // String id = uiElement.getAttribute("android:id");

            if (uiElement.hasAttribute("android:onClick")) {
                String onClickMethodName = uiElement.getAttribute("android:onClick");

                Log.bb(TAG, "Layout: " + layoutName);
                Log.bb(TAG, "    onclick: " + onClickMethodName);

                if (!handlerLayoutMap.containsKey(onClickMethodName)) {
                    handlerLayoutMap.put(onClickMethodName, new HashSet<String>());
                }

                handlerLayoutMap.get(onClickMethodName).add(xmlFile.getName());
            }
        }
    }

    public Set<String> getUILayoutDefinedHandlers() {
        return handlerLayoutMap.keySet();
    }
}
