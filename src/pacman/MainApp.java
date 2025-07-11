package pacman;

import javax.swing.*;

import pacman.containers.MenuComponent;

public class MainApp extends JFrame {
    public MainApp() {
        super();

        setBounds(100, 100, 465, 510);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().add(new MenuComponent(this).getContainer());

    }

    public static void main(String[] args) {
        new MainApp().setVisible(true);
    }

}

