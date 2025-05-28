package bgWork.handler;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import Listener.CPHActionListener;
import Pack.DragPack;
import Pack.SendText;
import bgWork.InitProcess;
import mod.instance.AssociationLine;
import mod.instance.BasicClass;
import mod.instance.CompositionLine;
import mod.instance.DependencyLine;
import mod.instance.GeneralizationLine;
import mod.instance.GroupContainer;
import mod.instance.UseCase;

public class CanvasPanelHandler extends PanelHandler
{
	Vector <JPanel>	members		= new Vector <>();
	Vector <JPanel>	selectComp	= new Vector <>();
	int				boundShift	= 10;

	public CanvasPanelHandler(JPanel Container, InitProcess process)
	{
		super(Container, process);
		boundDistance = 10;
		initContextPanel();
		Container.add(this.contextPanel);
	}

	@Override
	void initContextPanel()
	{
		JPanel fphContextPanel = core.getFuncPanelHandler().getContectPanel();
		contextPanel = new JPanel();
		contextPanel.setBounds(
				fphContextPanel.getLocation().x
						+ fphContextPanel.getSize().width + boundShift,
				fphContextPanel.getLocation().y, 800, 600);
		contextPanel.setLayout(null);
		contextPanel.setVisible(true);
		contextPanel.setBackground(Color.WHITE);
		contextPanel.setBorder(new LineBorder(Color.BLACK));
		contextPanel.addMouseListener(new CPHActionListener(this));
	}

	@Override
	public void ActionPerformed(MouseEvent e)
	{
		switch (core.getCurrentFuncIndex())
		{
			case 0:
				selectByClick(e);
				break;
			case 1:
			case 2:
			case 3:
			case 4: // DependencyLine case
				break;
			case 5:
			case 6:
				addObject(core.getCurrentFunc(), e.getPoint());
				break;
			default:
				break;
		}
		repaintComp();
	}

	public void ActionPerformed(DragPack dp)
	{
		switch (core.getCurrentFuncIndex())
		{
			case 0:
				selectByDrag(dp);
				break;
			case 1:
			case 2:
			case 3:
			case 4: // DependencyLine case
				addLine(core.getCurrentFunc(), dp);
				break;
			case 5:
			case 6:
				break;
			default:
				break;
		}
		repaintComp();
	}

	public void repaintComp()
	{
		for (int i = 0; i < members.size(); i ++)
		{
			members.elementAt(i).repaint();
		}
		contextPanel.updateUI();
	}

	void selectByClick(MouseEvent e) {
		boolean isSelect = false;
		selectComp = new Vector<>();
		
		for (int i = 0; i < members.size(); i++) {
			int componentType = core.isFuncComponent(members.elementAt(i));
			
			// 對於線條類型，只檢查端口點擊
			if (componentType >= 2 && componentType <= 4 || componentType == 6) {
				// 線條類型：只有點擊端口才能選擇
				if (isClickOnLinePort(members.elementAt(i), e.getPoint())) {
					setLineSelect(members.elementAt(i), true);
					selectComp.add(members.elementAt(i));
					isSelect = true;
				} else {
					setSelectAllType(members.elementAt(i), false);
				}
			} 
			// 對於其他類型，使用原來的邏輯
			else if (isInside(members.elementAt(i), e.getPoint()) && !isSelect) {
				switch (componentType) {
					case 0:
						((BasicClass) members.elementAt(i)).setSelect(true);
						selectComp.add(members.elementAt(i));
						isSelect = true;
						break;
					case 1:
						((UseCase) members.elementAt(i)).setSelect(true);
						selectComp.add(members.elementAt(i));
						isSelect = true;
						break;
					case 5:
						Point p = e.getPoint();
						p.x -= members.elementAt(i).getLocation().x;
						p.y -= members.elementAt(i).getLocation().y;
						if (groupIsSelect((GroupContainer) members.elementAt(i), p)) {
							((GroupContainer) members.elementAt(i)).setSelect(true);
							selectComp.add(members.elementAt(i));
							isSelect = true;
						} else {
							((GroupContainer) members.elementAt(i)).setSelect(false);
						}
						break;
					default:
						break;
				}
			} else {
				setSelectAllType(members.elementAt(i), false);
			}
		}
		repaintComp();
	}

	boolean isClickOnLinePort(JPanel lineComponent, Point clickPoint) {
		int portRadius = 12; // 增加端口點擊區域半徑
		
		switch (core.isFuncComponent(lineComponent)) {
			case 2: // AssociationLine
				return checkAssociationLinePort((AssociationLine) lineComponent, clickPoint, portRadius);
			case 3: // CompositionLine
				return checkCompositionLinePort((CompositionLine) lineComponent, clickPoint, portRadius);
			case 4: // GeneralizationLine
				return checkGeneralizationLinePort((GeneralizationLine) lineComponent, clickPoint, portRadius);
			case 6: // DependencyLine
				return checkDependencyLinePort((DependencyLine) lineComponent, clickPoint, portRadius);
			default:
				return false;
		}
	}

	boolean groupIsSelect(GroupContainer container, Point point)
	{
		for (int i = 0; i < container.getComponentCount(); i ++)
		{
			if (core.isGroupContainer(container.getComponent(i)))
			{
				point.x -= container.getComponent(i).getLocation().x;
				point.y -= container.getComponent(i).getLocation().y;
				if (groupIsSelect((GroupContainer) container.getComponent(i),
						point) == true)
				{
					return true;
				}
				else
				{
					point.x += container.getComponent(i).getLocation().x;
					point.y += container.getComponent(i).getLocation().y;
				}
			}
			else if (core.isJPanel(container.getComponent(i)))
			{
				if (isInside((JPanel) container.getComponent(i), point))
				{
					return true;
				}
			}
		}
		return false;
	}

	boolean selectByDrag(DragPack dp)
	{
		if (isInSelect(dp.getFrom()) == true)
		{
			// dragging components
			Dimension shift = new Dimension(dp.getTo().x - dp.getFrom().x,
					dp.getTo().y - dp.getFrom().y);
			for (int i = 0; i < selectComp.size(); i ++)
			{
				JPanel jp = selectComp.elementAt(i);
				jp.setLocation(jp.getLocation().x + shift.width,
						jp.getLocation().y + shift.height);
				if (jp.getLocation().x < 0)
				{
					jp.setLocation(0, jp.getLocation().y);
				}
				if (jp.getLocation().y < 0)
				{
					jp.setLocation(jp.getLocation().x, 0);
				}
			}
			return true;
		}
		if (dp.getFrom().x > dp.getTo().x && dp.getFrom().y > dp.getTo().y)
		{
			// drag right down from to left up
			groupInversSelect(dp);
			return true;
		}
		else if (dp.getFrom().x < dp.getTo().x && dp.getFrom().y < dp.getTo().y)
		{
			// drag from left up to right down
			groupSelect(dp);
			return true;
		}
		return false;
	}

	public void setGroup()
	{
		if (selectComp.size() > 1)
		{
			GroupContainer gContainer = new GroupContainer(core);
			gContainer.setVisible(true);
			Point p1 = new Point(selectComp.elementAt(0).getLocation().x,
					selectComp.elementAt(0).getLocation().y);
			Point p2 = new Point(selectComp.elementAt(0).getLocation().x,
					selectComp.elementAt(0).getLocation().y);
			Point testP;
			for (int i = 0; i < selectComp.size(); i ++)
			{
				testP = selectComp.elementAt(i).getLocation();
				if (p1.x > testP.x)
				{
					p1.x = testP.x;
				}
				if (p1.y > testP.y)
				{
					p1.y = testP.y;
				}
				if (p2.x < testP.x + selectComp.elementAt(i).getSize().width)
				{
					p2.x = testP.x + selectComp.elementAt(i).getSize().width;
				}
				if (p2.y < testP.y + selectComp.elementAt(i).getSize().height)
				{
					p2.y = testP.y + selectComp.elementAt(i).getSize().height;
				}
			}
			p1.x --;
			p1.y --;
			gContainer.setLocation(p1);
			gContainer.setSize(p2.x - p1.x + 2, p2.y - p1.y + 2);
			for (int i = 0; i < selectComp.size(); i ++)
			{
				JPanel temp = selectComp.elementAt(i);
				removeComponent(temp);
				gContainer.add(temp, i);
				temp.setLocation(temp.getLocation().x - p1.x,
						temp.getLocation().y - p1.y);
			}
			addComponent(gContainer);
			selectComp = new Vector <>();
			selectComp.add(gContainer);
			repaintComp();
		}
	}

	public void setUngroup()
	{
		int size = selectComp.size();
		for (int i = 0; i < size; i ++)
		{
			if (core.isGroupContainer(selectComp.elementAt(i)))
			{
				GroupContainer gContainer = (GroupContainer) selectComp
						.elementAt(i);
				Component temp;
				int j = 0;
				while (gContainer.getComponentCount() > 0)
				{
					temp = gContainer.getComponent(0);
					temp.setLocation(
							temp.getLocation().x + gContainer.getLocation().x,
							temp.getLocation().y + gContainer.getLocation().y);
					addComponent((JPanel) temp, j);
					selectComp.add((JPanel) temp);
					gContainer.remove(temp);
					j ++;
				}
				removeComponent(gContainer);
				selectComp.remove(gContainer);
			}
			repaintComp();
		}
	}

	void groupSelect(DragPack dp)
	{
		JPanel jp = new JPanel();
		jp.setLocation(dp.getFrom());
		jp.setSize(Math.abs(dp.getTo().x - dp.getFrom().x),
				Math.abs(dp.getTo().y - dp.getFrom().x));
		selectComp = new Vector <>();
		for (int i = 0; i < members.size(); i ++)
		{
			if (isInside(jp, members.elementAt(i)) == true)
			{
				selectComp.add(members.elementAt(i));
				setSelectAllType(members.elementAt(i), true);
			}
			else
			{
				setSelectAllType(members.elementAt(i), false);
			}
		}
	}

	void groupInversSelect(DragPack dp)
	{
		JPanel jp = new JPanel();
		jp.setLocation(dp.getTo());
		jp.setSize(Math.abs(dp.getTo().x - dp.getFrom().x),
				Math.abs(dp.getTo().y - dp.getFrom().x));
		selectComp = new Vector <>();
		for (int i = 0; i < members.size(); i ++)
		{
			if (isInside(jp, members.elementAt(i)) == false)
			{
				selectComp.add(members.elementAt(i));
				setSelectAllType(members.elementAt(i), true);
			}
			else
			{
				setSelectAllType(members.elementAt(i), false);
			}
		}
	}

	boolean isInSelect(Point point)
	{
		for (int i = 0; i < selectComp.size(); i ++)
		{
			if (isInside(selectComp.elementAt(i), point) == true)
			{
				return true;
			}
		}
		return false;
	}

	void addLine(JPanel funcObj, DragPack dPack) {
		for (int i = 0; i < members.size(); i++) {
			if (isInside(members.elementAt(i), dPack.getFrom())) {
				dPack.setFromObj(members.elementAt(i));
			}
			if (isInside(members.elementAt(i), dPack.getTo())) {
				dPack.setToObj(members.elementAt(i));
			}
		}
		if (dPack.getFromObj() == dPack.getToObj()
				|| dPack.getFromObj() == contextPanel
				|| dPack.getToObj() == contextPanel) {
			return;
		}
		switch (members.size()) {
			case 0:
			case 1:
				break;
			default:
				switch (core.isLine(funcObj)) {
					case 0:
						((AssociationLine) funcObj).setConnect(dPack);
						break;
					case 1:
						((CompositionLine) funcObj).setConnect(dPack);
						break;
					case 2:
						((GeneralizationLine) funcObj).setConnect(dPack);
						break;
					case 3:
						((DependencyLine) funcObj).setConnect(dPack);
						break;
					default:
						break;
				}
				contextPanel.add(funcObj, 0);
				members.insertElementAt(funcObj, 0); // 確保添加到 members 列表
				break;
		}
	}

	void addObject(JPanel funcObj, Point point)
	{
		if (members.size() > 0)
		{
			members.insertElementAt(funcObj, 0);
		}
		else
		{
			members.add(funcObj);
		}
		members.elementAt(0).setLocation(point);
		members.elementAt(0).setVisible(true);
		contextPanel.add(members.elementAt(0), 0);
	}

	public boolean isInside(JPanel container, Point point)
	{
		Point cLocat = container.getLocation();
		Dimension cSize = container.getSize();
		if (point.x >= cLocat.x && point.y >= cLocat.y)
		{
			if (point.x <= cLocat.x + cSize.width
					&& point.y <= cLocat.y + cSize.height)
			{
				return true;
			}
		}
		return false;
	}

	public boolean isInside(JPanel container, JPanel test)
	{
		Point cLocat = container.getLocation();
		Dimension cSize = container.getSize();
		Point tLocat = test.getLocation();
		Dimension tSize = test.getSize();
		if (cLocat.x <= tLocat.x && cLocat.y <= tLocat.y)
		{
			if (cLocat.x + cSize.width >= tLocat.x + tSize.width
					&& cLocat.y + cSize.height >= tLocat.y + tSize.height)
			{
				return true;
			}
		}
		return false;
	}

	public JPanel getSingleSelectJP()
	{
		if (selectComp.size() == 1)
		{
			return selectComp.elementAt(0);
		}
		return null;
	}

	public void setContext(SendText tr)
	{
		System.out.println(tr.getText());
		try
		{
			switch (core.isClass(tr.getDest()))
			{
				case 0:
					((BasicClass) tr.getDest()).setText(tr.getText());
					break;
				case 1:
					((UseCase) tr.getDest()).setText(tr.getText());
					break;
				default:
					break;
			}
		}
		catch (Exception e)
		{
			System.err.println("CPH error");
		}
	}

	void addComponent(JPanel comp)
	{
		contextPanel.add(comp, 0);
		members.insertElementAt(comp, 0);
	}

	void addComponent(JPanel comp, int index)
	{
		contextPanel.add(comp, index);
		members.insertElementAt(comp, index);
	}

	public void removeComponent(JPanel comp)
	{
		contextPanel.remove(comp);
		members.remove(comp);
	}

	void setSelectAllType(Object obj, boolean isSelect)
	{
		switch (core.isFuncComponent(obj))
		{
			case 0:
				((BasicClass) obj).setSelect(isSelect);
				break;
			case 1:
				((UseCase) obj).setSelect(isSelect);
				break;
			case 2:
				((AssociationLine) obj).setSelect(isSelect);
				break;
			case 3:
				((CompositionLine) obj).setSelect(isSelect);
				break;
			case 4:
				((GeneralizationLine) obj).setSelect(isSelect);
				break;
			case 5:
				((GroupContainer) obj).setSelect(isSelect);
				break;
			case 6:
				((DependencyLine) obj).setSelect(isSelect);
				break;
			default:
				break;
		}
	}

	public Point getAbsLocation(Container panel)
	{
		Point location = panel.getLocation();
		while (panel.getParent() != contextPanel)
		{
			panel = panel.getParent();
			location.x += panel.getLocation().x;
			location.y += panel.getLocation().y;
		}
		return location;
	}
	/**
	 * 檢查AssociationLine的端口
	 */
	boolean checkAssociationLinePort(AssociationLine line, Point clickPoint, int radius) {
		// 獲取線條的起點和終點
		Point fromPoint = getLineFromPoint(line);
		Point toPoint = getLineToPoint(line);
		
		// 檢查是否點擊在起點或終點的範圍內
		return isPointInCircle(clickPoint, fromPoint, radius) || 
			isPointInCircle(clickPoint, toPoint, radius);
	}

	/**
	 * 檢查CompositionLine的端口
	 */
	boolean checkCompositionLinePort(CompositionLine line, Point clickPoint, int radius) {
		Point fromPoint = getLineFromPoint(line);
		Point toPoint = getLineToPoint(line);
		
		return isPointInCircle(clickPoint, fromPoint, radius) || 
			isPointInCircle(clickPoint, toPoint, radius);
	}

	/**
	 * 檢查GeneralizationLine的端口
	 */
	boolean checkGeneralizationLinePort(GeneralizationLine line, Point clickPoint, int radius) {
		Point fromPoint = getLineFromPoint(line);
		Point toPoint = getLineToPoint(line);
		
		return isPointInCircle(clickPoint, fromPoint, radius) || 
			isPointInCircle(clickPoint, toPoint, radius);
	}

	/**
	 * 檢查DependencyLine的端口
	 */
	boolean checkDependencyLinePort(DependencyLine line, Point clickPoint, int radius) {
		Point fromPoint = getLineFromPoint(line);
		Point toPoint = getLineToPoint(line);
		
		return isPointInCircle(clickPoint, fromPoint, radius) || 
			isPointInCircle(clickPoint, toPoint, radius);
	}

	/**
	 * 檢查點是否在圓形區域內
	 */
	boolean isPointInCircle(Point clickPoint, Point centerPoint, int radius) {
		double distance = Math.sqrt(Math.pow(clickPoint.x - centerPoint.x, 2) + 
								Math.pow(clickPoint.y - centerPoint.y, 2));
		return distance <= radius;
	}
	
	/**
	 * 獲取線條的起點座標
	 */
	Point getLineFromPoint(JPanel lineComponent) {
		// 這需要根據您的線條類別實現來調整
		// 假設線條類別有getFromPoint()方法
		switch (core.isFuncComponent(lineComponent)) {
			case 2: // AssociationLine
				return ((AssociationLine) lineComponent).getFromPoint();
			case 3: // CompositionLine
				return ((CompositionLine) lineComponent).getFromPoint();
			case 4: // GeneralizationLine
				return ((GeneralizationLine) lineComponent).getFromPoint();
			case 6: // DependencyLine
				return ((DependencyLine) lineComponent).getFromPoint();
			default:
				return new Point(0, 0);
		}
	}

	/**
	 * 獲取線條的終點座標
	 */
	Point getLineToPoint(JPanel lineComponent) {
		switch (core.isFuncComponent(lineComponent)) {
			case 2: // AssociationLine
				return ((AssociationLine) lineComponent).getToPoint();
			case 3: // CompositionLine
				return ((CompositionLine) lineComponent).getToPoint();
			case 4: // GeneralizationLine
				return ((GeneralizationLine) lineComponent).getToPoint();
			case 6: // DependencyLine
				return ((DependencyLine) lineComponent).getToPoint();
			default:
				return new Point(0, 0);
		}
	}

	/**
	 * 設置線條選中狀態
	 */
	void setLineSelect(JPanel lineComponent, boolean isSelect) {
		switch (core.isFuncComponent(lineComponent)) {
			case 2:
				((AssociationLine) lineComponent).setSelect(isSelect);
				break;
			case 3:
				((CompositionLine) lineComponent).setSelect(isSelect);
				break;
			case 4:
				((GeneralizationLine) lineComponent).setSelect(isSelect);
				break;
			case 6:
				((DependencyLine) lineComponent).setSelect(isSelect);
				break;
		}
	}
}
