package servicio;

import javax.mail.*;
import java.util.*;
import java.util.stream.Collectors;

public class EtiquetadorGmail 
{
    private static final String HOST = "imap.gmail.com";
    private static final String USER = "cuentaspspmarcosdc@gmail.com";
    private static final String PASSWORD = "oahr ddnh ztko vahk";
    private static final int NUM_MAX_MENSAJES = 7;
    private static final String LABEL_DONE = "Done";
    private static final String LABEL_WORK_IN_PROGRESS = "Work.in.progress";
    private static final String LABEL_TO_BE_DONE = "To.be.done";

    private Session creaSesion()
    {
        Properties propiedades = new Properties();
        propiedades.put("mail.store.protocol", "imaps");
        propiedades.put("mail.imap.ssl.enable", "true");
        propiedades.put("mail.imap.ssl.trust", "*");
        propiedades.put("mail.imap.auth", "true");
        propiedades.put("mail.imap.port", "993");

        return Session.getInstance(propiedades);
    }

    public void etiquetasEmails() 
    {
        Store store = null;
        Folder inbox = null;
        try 
        {
            store = creaSesion().getStore("imaps");
            store.connect(HOST, USER, PASSWORD);

            crearCarpetaEtiqueta(store, LABEL_DONE);
            crearCarpetaEtiqueta(store, LABEL_WORK_IN_PROGRESS);
            crearCarpetaEtiqueta(store, LABEL_TO_BE_DONE);

            inbox = store.getDefaultFolder().getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] mensajes = inbox.getMessages();
            int totalMensajes = Math.min(mensajes.length, NUM_MAX_MENSAJES);

            for (int i = 0; i < totalMensajes; i++) 
            {
                String etiqueta = consigueEtiquetaPorIndice(i);
                copiarMensajeAEtiqueta(store, inbox, mensajes[i], etiqueta);
            }
        } 
        catch (Exception e) 
        {
            System.out.println(e.getMessage());
        } 
        finally 
        {
            cerrarRecursos(inbox, store);
        }
    }

    private String consigueEtiquetaPorIndice(int indice) 
    {
        if (indice < 3)
        {
            return LABEL_DONE;
        }
        if (indice == 3)
        {
            return LABEL_WORK_IN_PROGRESS;
        }
        return LABEL_TO_BE_DONE;
    }

    private void copiarMensajeAEtiqueta(Store store, Folder inbox, Message mensaje, String etiqueta) throws MessagingException 
    {
        Folder carpetaRaiz = store.getDefaultFolder();
        Folder carpetaEtiqueta = carpetaRaiz.getFolder(etiqueta);
        if (carpetaEtiqueta.exists()) inbox.copyMessages(new Message[]{mensaje}, carpetaEtiqueta);
    }

    private void crearCarpetaEtiqueta(Store store, String etiqueta) throws MessagingException
    {
        Folder carpetaRaiz = store.getDefaultFolder();
        Folder carpetaEtiqueta = carpetaRaiz.getFolder(etiqueta);
        if (!carpetaEtiqueta.exists()) carpetaEtiqueta.create(Folder.HOLDS_MESSAGES);
    }

    public Map<String, List<String>> buscarMensajesEtiquetados()
    {
        Map<String, List<String>> mensajesEtiquetados = new HashMap<>();
        Store store = null;
        try
        {
            store = creaSesion().getStore("imaps");
            store.connect(HOST, USER, PASSWORD);

            for (String etiqueta : Arrays.asList(LABEL_DONE, LABEL_WORK_IN_PROGRESS, LABEL_TO_BE_DONE))
            {
                Folder carpetaRaiz = store.getDefaultFolder();
                Folder carpeta = carpetaRaiz.getFolder(etiqueta);

                if (carpeta.exists())
                {
                    carpeta.open(Folder.READ_ONLY);
                    List<String> subjects = Arrays.stream(carpeta.getMessages())
                            .map(m -> {
                                try
                                {
                                    return m.getSubject() != null ? m.getSubject() : "(Sin Asunto)";
                                }
                                catch (MessagingException e)
                                {
                                    return "(Error al obtener asunto)";
                                }
                            })
                            .collect(Collectors.toList());
                    mensajesEtiquetados.put(etiqueta, subjects);
                    carpeta.close(false);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            if (store != null)
            {
                try
                {
                    store.close();
                }
                catch (MessagingException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
        return mensajesEtiquetados;
    }

    private void cerrarRecursos(Folder carpeta, Store store)
    {
        try
        {
            if (carpeta != null && carpeta.isOpen())
            {
                carpeta.close(false);
            }
            if (store != null && store.isConnected())
            {
                store.close();
            }
        }
        catch (MessagingException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public Store getStoreConnection() throws MessagingException
    {
        Store store = creaSesion().getStore("imaps");
        store.connect(HOST, USER, PASSWORD);
        return store;
    }
}