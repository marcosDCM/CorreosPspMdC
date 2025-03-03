package interfaz;

import servicio.EtiquetadorGmail;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

public class InterfazEtiquetasGmail extends JFrame
{
    private final EtiquetadorGmail etiquetador = new EtiquetadorGmail();
    private final JPanel panelPrincipal = new JPanel(new BorderLayout());
    private final JButton etiquetaBoton = new JButton("Etiquetar Correos");
    private final JTextArea areaListaEmails = new JTextArea();

    public InterfazEtiquetasGmail()
    {
        setTitle("Etiquetador de Gmail");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        initUI();
        setLocationRelativeTo(null);
    }

    private void initUI()
    {
        areaListaEmails.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaListaEmails);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        panelPrincipal.add(etiquetaBoton, BorderLayout.SOUTH);
        add(panelPrincipal);

        mostrarCorreosEtiquetados();

        etiquetaBoton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                etiquetaBoton.setEnabled(false);
                SwingWorker<Void, Void> worker = new SwingWorker<>()
                {
                    @Override
                    protected Void doInBackground()
                    {
                        etiquetador.etiquetasEmails();
                        return null;
                    }

                    @Override
                    protected void done()
                    {
                        JOptionPane.showMessageDialog(
                                InterfazEtiquetasGmail.this,
                                "Correos etiquetados correctamente.",
                                "Éxito",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        mostrarCorreosEtiquetados();
                        etiquetaBoton.setEnabled(true);
                    }
                };
                worker.execute();
            }
        });
    }

    private void mostrarCorreosEtiquetados()
    {
        SwingWorker<Map<String, List<String>>, Void> worker = new SwingWorker<>()
        {
            @Override
            protected Map<String, List<String>> doInBackground()
            {
                return etiquetador.buscarMensajesEtiquetados();
            }

            @Override
            protected void done()
            {
                try
                {
                    Map<String, List<String>> messages = get();
                    StringBuilder sb = new StringBuilder();

                    for (Map.Entry<String, List<String>> entry : messages.entrySet())
                    {
                        sb.append("=== ").append(entry.getKey()).append(" ===\n");
                        for (String subject : entry.getValue())
                        {
                            sb.append("- ").append(subject).append("\n");
                        }
                        sb.append("\n");
                    }

                    areaListaEmails.setText(sb.toString());
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(
                            InterfazEtiquetasGmail.this,
                            "Error: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }
}