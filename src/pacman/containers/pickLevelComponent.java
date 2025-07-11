package pacman.containers;

import pacman.containers.components.MyButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class pickLevelComponent extends JFrame {
    private Container container = this.getContentPane();
    public pickLevelComponent(JFrame mainPane){
choseMap(mainPane);
    }
        public void choseMap(JFrame mainPane){
            getContentPane().removeAll();
            this.getContentPane().setBackground(Color.darkGray);
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            MyButton myButton = new MyButton(mainPane);
            container.add(Box.createVerticalStrut(130));
            for (JButton btn : myButton.getBoardsBtnList()) {
                container.add(btn);
                container.add(Box.createVerticalStrut(30));
            }
            revalidate();
            repaint();
        }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }
    }



