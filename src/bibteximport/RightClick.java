package bibteximport;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

public class RightClick extends MouseAdapter {
	private JPopupMenu popup = new JPopupMenu();

	private AbstractAction cutAction;
	private AbstractAction copyAction;
	private AbstractAction pasteAction;
	private AbstractAction undoAction;
	private AbstractAction selectAllAction;

	private JTextComponent component;
	private String savedString = "";
	private Actions lastActionSelected;

	private enum Actions { UNDO, CUT, COPY, PASTE, SELECT_ALL };

	public RightClick() {
		undoAction = new AbstractAction("Undo") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				component.setText("");
				component.replaceSelection(savedString);

				lastActionSelected = Actions.UNDO;
			}
		};

		popup.add(undoAction);
		popup.addSeparator();

		cutAction = new AbstractAction("Cut") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				lastActionSelected = Actions.CUT;
				savedString = component.getText();
				component.cut();
			}
		};

		popup.add(cutAction);

		copyAction = new AbstractAction("Copy") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				lastActionSelected = Actions.COPY;
				component.copy();
			}
		};

		popup.add(copyAction);

		pasteAction = new AbstractAction("Paste") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				lastActionSelected = Actions.PASTE;
				savedString = component.getText();
				component.paste();
			}
		};

		popup.add(pasteAction);
		popup.addSeparator();

		selectAllAction = new AbstractAction("Select All") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				lastActionSelected = Actions.SELECT_ALL;
				component.selectAll();
			}
		};

		popup.add(selectAllAction);
	}

	@Override
	public void mouseClicked(MouseEvent ev) {
		if (ev.getModifiers() != InputEvent.BUTTON3_MASK)
			return;
		
		component = (JTextComponent) ev.getSource();
		component.requestFocus();

		boolean enabled = component.isEnabled();
		boolean editable = component.isEditable();
		boolean nonempty = !(component.getText() == null || component.getText().equals(""));
		boolean marked = component.getSelectedText() != null;

		boolean pasteAvailable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor);

		undoAction.setEnabled(enabled && editable && (lastActionSelected == Actions.CUT || lastActionSelected == Actions.PASTE));
		cutAction.setEnabled(enabled && editable && marked);
		copyAction.setEnabled(enabled && marked);
		pasteAction.setEnabled(enabled && editable && pasteAvailable);
		selectAllAction.setEnabled(enabled && nonempty);

		int nx = ev.getX();

		if (nx > 500) {
			nx = nx - popup.getSize().width;
		}

		popup.show(ev.getComponent(), nx, ev.getY() - popup.getSize().height);
	}

}
