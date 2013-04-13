package cz.muni.fi.rhqeditor.core.rhqmodel;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.muni.fi.rhqeditor.core.Activator;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;

class RhqModel {
	private Map<String, RhqTask> fModel;
	private List<String> fReplacements;
	private static final RhqModel instance = new RhqModel();
	
	private RhqModel(){
		fModel = readDocument();
	}
	
	public static RhqModel getInstance(){
		return instance;
	}
	
	public Map<String,RhqTask> getModel(){
		return fModel;
	}
	public List<String> getReplacements(){
		return fReplacements;
	}
	
	
	
	/**
	 * read xml file with model of rhq tasks and saves it into map
	 * @return
	 */
	private Map<String, RhqTask> readDocument() {
		try {
			
			URL fXmlFile = Activator.getFileURL(RhqConstants.RHQ_MODEL_SOURCEFILE);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile.toString());
			doc.getDocumentElement().normalize();

			Map<String,RhqEntity> entities = new HashMap<>();
			//find all entities in file
			NodeList entity = doc.getElementsByTagName("entity");
			for(int i = 0; i != entity.getLength(); i++){
				RhqEntity currentEntity = new RhqEntity();
				Element elEntity = (Element) entity.item(i);
				currentEntity.setName(elEntity.getAttribute("name"));
				System.out.println(currentEntity.getName());
				NodeList values = elEntity.getChildNodes();
				for(int j = 0; j != values.getLength(); j++){
					if(values.item(j).getNodeType() == Node.ELEMENT_NODE)
						currentEntity.getValues().add(((Element)values.item(j)).getAttribute("name"));
				}
				entities.put(currentEntity.getName(), currentEntity);
			}
			
			//find all replacement variables
			fReplacements = new ArrayList<>();
			NodeList replacements = doc.getElementsByTagName("replacemnet");
			for(int i = 0; i != replacements.getLength(); i++){
				if(replacements.item(i).getNodeType() == Node.ELEMENT_NODE){
					fReplacements.add(((Element)replacements.item(i)).getAttribute("name"));
				}
			}
		
			
			Map<String, RhqTask> tasks = new HashMap<>();
			NodeList task = doc.getElementsByTagName("task");
			// iterates over all tasks
			for (int i = 0; i != task.getLength(); i++) {
				RhqTask currentTask = new RhqTask();
				Node node = task.item(i);
				Element taskHead;
				taskHead = (Element) node;
				currentTask.setName(taskHead.getAttribute("name"));
				currentTask.setPaired((taskHead.getAttribute("paired")
						.equalsIgnoreCase("true") ? true : false));

				tasks.put(currentTask.getName(), currentTask);
				NodeList taskChildren = taskHead.getChildNodes();
				// iterates over children of current task
				for (int j = 0; j != taskChildren.getLength(); j++) {

					if (taskChildren.item(j).getNodeType() == Node.ELEMENT_NODE) {
						switch (taskChildren.item(j).getNodeName()) {
						case "antparents":
							NodeList antParents = taskChildren.item(j)
									.getChildNodes();
							Set<String> setAntParents = new HashSet<>();
							for (int k = 0; k != antParents.getLength(); k++) {
								if (antParents.item(k).getNodeType() == Node.ELEMENT_NODE) {
									Element name = (Element) antParents.item(k);
									//check whether can be in all ant tasks
									if(name.getAttribute("name").equals(RhqConstants.RHQ_ALL_POSSIBLE_PARENTS)){
										currentTask.setCanBePlacedInAnyTask(true);
									} else {
										setAntParents.add(name.getAttribute("name"));
									}
										
								}
							}
							currentTask.setAntParents(setAntParents);
							break;

						case "attributes":
							NodeList attributes = taskChildren.item(j)
									.getChildNodes();
							Set<RhqAttribute> setAttributes = new HashSet<>();
							for (int k = 0; k != attributes.getLength(); k++) {
								RhqAttribute currentAttribute = new RhqAttribute();
								if (attributes.item(k).getNodeType() == Node.ELEMENT_NODE) {
									Element el = (Element) attributes.item(k);
									currentAttribute.setName(el
											.getAttribute("name"));
									currentAttribute.setRequired((el
											.getAttribute("required")
											.equalsIgnoreCase("true") ? true
											: false));
									currentAttribute.setVisible((el
											.getAttribute("visible")
											.equalsIgnoreCase("true") ? true
											: false));
									String entityName = el.getAttribute("entity");
									currentAttribute.setEntity(entities.get(entityName));
									currentAttribute.setDescription(el
											.getTextContent());
									setAttributes.add(currentAttribute);
								}
								currentTask.setAttributes(setAttributes);
							}

							break;

						case "description":
							currentTask.setDescription(taskChildren.item(j)
									.getTextContent());
							break;

						}
					}

				}

			}

			// complete parent and children references
			NodeList parents = doc.getElementsByTagName("parent");
			for (int parentIndex = 0; parentIndex != parents.getLength(); parentIndex++) {
				Element elParent = (Element) parents.item(parentIndex);
				if (elParent.getParentNode().getNodeName()
						.equalsIgnoreCase("rhqparents")) {
					RhqTask parentTask = tasks.get(elParent
							.getAttribute("name"));
					RhqTask descendentTask = tasks.get(((Element) elParent
							.getParentNode().getParentNode())
							.getAttribute("name"));
					parentTask.getDescendents().add(descendentTask);
					descendentTask.getParents().add(parentTask);

				}
			}
			
			//add ant children
			NodeList nodes = doc.getElementsByTagName("antchild");
			for(int i = 0; i != nodes.getLength(); i++){
				RhqTask parentTask = tasks.get(((Element)nodes.item(i).getParentNode().getParentNode()).getAttribute("name"));
				parentTask.getAntChildren().add(((Element)nodes.item(i)).getAttribute("name"));
			}

			return tasks;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
