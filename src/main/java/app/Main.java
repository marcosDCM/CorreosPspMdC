package app;

import interfaz.InterfazEtiquetasGmail;

import java.awt.*;

public class Main
{
    public static void main(String[] args)
    {
        EventQueue.invokeLater(() -> new InterfazEtiquetasGmail().setVisible(true));
    }
}
