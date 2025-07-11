package pacman.containers;

import  pacman.containers.components.MyButton;

import javax.swing.*;
import java.awt.*;

public class MenuComponent extends JFrame {
    private Container container = this.getContentPane();

    public MenuComponent(JFrame mainPane) {
        initMenu(mainPane);
    }

    public void initMenu(JFrame mainPane) {
        getContentPane().removeAll();
        this.getContentPane().setBackground(Color.darkGray);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        MyButton myButton = new MyButton(mainPane);
        container.add(Box.createVerticalStrut(130));
        for (JButton btn : myButton.getMainMenuBtnList()) {
            container.add(btn);
            container.add(Box.createVerticalStrut(30));
        }
        revalidate();
        repaint();
    }

    public Container getContainer() {
        return container;
    }
}