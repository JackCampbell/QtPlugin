package jack.campbell.work.qt;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.jetbrains.cidr.cpp.CPPLog;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


class ResourceItem {
	public String alias = null;
	public String path;
	public ResourceItem(String path) {
		this.path = path;
	}
	public String getAlias() {
		return alias == null ? path : alias;
	}

	@Override
	public String toString() {
		return path;
	}
}

class ResourceCollection {
	public String prefix = null;
	public List<ResourceItem> list = new ArrayList<ResourceItem>();
	public boolean IsRoot() {
		return prefix == null;
	}

	@Override
	public String toString() {
		return (prefix == null ? prefix : "Global");
	}
}
/**
 * The methods in this class allow the JTree component to traverse
 * the file system tree and display the files and directories.
 **/
class ResourceTreeModel implements TreeModel {
	public List<ResourceCollection> collections;
	public ResourceTreeModel() {
		collections = new ArrayList<ResourceCollection>();
	}
	@Override
	public Object getRoot() {
		return this;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if(parent instanceof ResourceTreeModel) {
			return ((ResourceTreeModel)parent).collections.get(index);
		}
		if(parent instanceof ResourceCollection) {
			return ((ResourceCollection)parent).list.get(index);
		}
		return null;
	}

	@Override
	public int getChildCount(Object parent) {
		if(parent instanceof ResourceTreeModel) {
			return ((ResourceTreeModel)parent).collections.size();
		}
		if(parent instanceof ResourceCollection) {
			return ((ResourceCollection)parent).list.size();
		}
		return 0;
	}

	@Override
	public boolean isLeaf(Object node) {
		if(node instanceof ResourceItem) {
			return true;
		}
		return false;
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if(parent instanceof ResourceTreeModel) {
			return ((ResourceTreeModel)parent).collections.indexOf(child);
		}
		if(parent instanceof ResourceCollection) {
			return ((ResourceCollection)parent).list.indexOf(child);
		}
		return -1;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {

	}

	@Override
	public void addTreeModelListener(TreeModelListener listener) {

	}

	@Override
	public void removeTreeModelListener(TreeModelListener listener) {

	}

	@Override
	public String toString() {
		return "Root";
	}
}

/**
 * Created by jack on 4/20/17.
 */
public class QtResourceView implements ToolWindowFactory, TreeSelectionListener, ActionListener {
	private JPanel mainPanel;
	private JTree resTree;
	private JComboBox comboBox1;
	private JTextField fldAlias;
	private JButton removeButton;
	private TextFieldWithBrowseButton fldPath;
	private JCheckBox copyCheckBox;

	public QtResourceView() {

		resTree.addTreeSelectionListener(this);
		removeButton.addActionListener(this);
	}


	private ToolWindow resourceViewToolWindow;
	private Project project;
	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		this.resourceViewToolWindow = toolWindow;
		this.project = project;

		ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
		Content content = contentFactory.createContent(mainPanel, "", false);
		toolWindow.getContentManager().addContent(content);

		try {
			LoadFile(project);
		} catch (Exception e) {
			CPPLog.LOG.warn(e);
		}
	}



	private void LoadFile(Project project) throws Exception {
		VirtualFile vfile = project.getBaseDir().findChild("resources.qrc");
		if(vfile == null) {
			return;
		}

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(vfile.getInputStream());
		//optional, but recommended
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();

		ResourceTreeModel model = new ResourceTreeModel();
		try {
			NodeList collectionNodeList = doc.getElementsByTagName("qresource");
			for(int i = 0; i < collectionNodeList.getLength(); i++) {
				Node collectionNode = collectionNodeList.item(i);
				if (collectionNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				Element collectionElement = (Element)collectionNode;
				ResourceCollection collection = new ResourceCollection();

				String prefix = collectionElement.getAttribute("prefix");
				if(prefix != null || !prefix.isEmpty()) {
					collection.prefix = prefix;
				}


				NodeList itemNodeList = collectionElement.getElementsByTagName("file");
				for(int j = 0; j < itemNodeList.getLength(); j++) {
					Node fileNode = (Node)itemNodeList.item(j);
					if(fileNode.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}

					Element itemElement = (Element) fileNode;
					String path = itemElement.getTextContent();
					String alias = collectionElement.getAttribute("alias");
					ResourceItem item = new ResourceItem(path);
					if(alias != null || !alias.isEmpty()) {
						item.alias = alias;
					}
					collection.list.add(item);
				}
				model.collections.add(collection);
			}
		} catch (Exception e) {
			CPPLog.LOG.warn(e);
		} finally {
			resTree.setModel(model);
			resTree.updateUI();
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		String path = e.getPath().toString();
		Object object = e.getNewLeadSelectionPath().getLastPathComponent();
		if(object instanceof ResourceItem) {
			ResourceItem item = (ResourceItem)object;
			fldPath.setText(item.path);
			fldAlias.setText(item.alias);

			fldAlias.setEnabled(true);
			fldPath.setEnabled(true);
		} else if(object instanceof ResourceCollection) {
			ResourceCollection item = (ResourceCollection)object;
			fldPath.setText("");
			fldAlias.setText(item.prefix);

			fldAlias.setEnabled(true);
			fldPath.setEnabled(false);
		} else {
			fldAlias.setEnabled(false);
			fldPath.setEnabled(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == removeButton) {
			TreePath path = resTree.getAnchorSelectionPath();
			resTree.removeSelectionPath(path);
		}
	}
}
