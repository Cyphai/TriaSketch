package com.triades.gui;



import com.google.gson.Gson;
import com.triades.model.ProgramData;
import com.triades.model.Schema;
import com.triades.tools.IconLoader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Iterator;

public class MainFrame extends JFrame {
   private static final long serialVersionUID = 1L;
   private File currentFile = null;
   private File currentDirectory = null;
   private Schema currentSchema = null;
   private SchemaPanel currentPanel = null;
   private JMenu lastFilesMenu;
   private ProgramData programData;
   private static MainFrame singleton;

   private MainFrame() {
      super("TriaSketch");
      this.setSize(1000, 900);
      this.setDefaultCloseOperation(0);

      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      this.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            MainFrame.this.saveAndExit();
         }
      });
      this.setIconImage(IconLoader.getTinyIcon("note").getImage());
      ToolTipManager.sharedInstance().setInitialDelay(50);
      this.programData = ProgramData.loadProgramData();
      this.buildMenu();
   }

   public static void main(String[] args) {
      IconLoader.initializeHashMap();
      singleton = new MainFrame();
      singleton.setVisible(true);
      singleton.setExtendedState(6);
   }

   public static MainFrame getSingleton() {
      return singleton;
   }

   public void updateTitle(String newTitle) {
      if (this.currentFile != null) {
         newTitle = new String(newTitle + " - " + this.currentFile.getAbsolutePath());
      }

      if (newTitle.length() > 0) {
         this.setTitle("TriaSketch - " + newTitle);
      } else {
         this.setTitle("TriaSketch");
      }

   }

   public void openSchema(Schema schema) {
      this.getContentPane().removeAll();
      this.currentSchema = schema;
      this.currentPanel = new SchemaPanel(this.currentSchema);
      this.updateTitle(schema.getDatas().getName());
      this.getContentPane().add(this.currentPanel);
      this.validate();
      this.repaint();
   }

   protected boolean saveSchema() {
      if (this.currentSchema == null) {
         JOptionPane.showMessageDialog(this, "Aucun schéma ouvert, impossible d'enregistrer");
         return false;
      } else {
         if (this.currentFile == null) {
            File baseFile = new File(this.convertTitleToFileName());
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(this.currentDirectory);
            fileChooser.setFileFilter(new FileNameExtensionFilter("Schéma", new String[]{"scm"}));
            fileChooser.setSelectedFile(baseFile);
            int result = fileChooser.showSaveDialog(this);
            if (result != 0) {
               return false;
            }

            this.currentFile = fileChooser.getSelectedFile();
            if (!this.currentFile.getName().endsWith(".scm")) {
               this.currentFile = new File(this.currentFile.getAbsolutePath() + ".scm");
            }

            this.currentDirectory = this.currentFile.getParentFile();
            if (this.currentFile.exists()) {
               int r = JOptionPane.showConfirmDialog(this, "Le fichier " + this.currentFile.getName() + " existe déjà, êtes-vous sur de vouloir l'écraser?");
               if (r == 1) {
                  this.currentFile = null;
                  return this.saveSchema();
               }

               if (r != 0) {
                  return false;
               }
            }
         }

         try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(this.currentFile), StandardCharsets.ISO_8859_1);
            Gson gson = new Gson();
            gson.toJson((Object)this.currentSchema, (Appendable)writer);
            writer.close();
         } catch (IOException var5) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement du fichier : \n" + var5.toString(), "Erreur", 0);
            return false;
         }

         this.addFileToLastFileList(this.currentFile);
         return true;
      }
   }

   protected String convertTitleToFileName() {
      String result = this.currentSchema.getDatas().getName();
      result = result.toLowerCase().trim();
      result = result.replace(' ', '_');
      result = result.replace('\'', '_');
      result = Normalizer.normalize(result, Form.NFD).replaceAll("[̀-ͯ]", "");
      result = result + ".scm";
      return result;
   }

   protected void loadSchema(File fileToLoad) {
      Schema oldSchema = this.currentSchema;
      if (this.currentSchema != null) {
         int r = JOptionPane.showConfirmDialog(this, "Un schéma est déjà ouvert, voulez-vous l'enregistrer avant d'en ouvrir un autre?", "Attention", 1, 2);
         if (r == 0) {
            if (!this.saveSchema()) {
               return;
            }
         } else if (r != 1) {
            return;
         }

         this.currentSchema = null;
      }

      if (fileToLoad == null) {
         JFileChooser fileChooser = new JFileChooser(this.currentDirectory);
         fileChooser.setFileFilter(new FileNameExtensionFilter("Schéma", new String[]{"scm"}));
         int result = fileChooser.showOpenDialog(this);
         if (result != 0) {
            this.currentSchema = oldSchema;
            return;
         }

         fileToLoad = fileChooser.getSelectedFile();
         this.currentDirectory = fileChooser.getCurrentDirectory();
      }

      try {
         FileInputStream is = new FileInputStream(fileToLoad);
         InputStreamReader isr = new InputStreamReader(is, StandardCharsets.ISO_8859_1);
         BufferedReader buffReader = new BufferedReader(isr);
         Gson gson = new Gson();
         Schema loadedSchema = (Schema)gson.fromJson((Reader)buffReader, (Class)Schema.class);
         if (loadedSchema == null) {
            JOptionPane.showMessageDialog(this, "Erreur durant l'ouverture du fichier, aucun schéma contenu dans le fichier", "Erreur", 0);
            this.currentSchema = oldSchema;
            return;
         }

         buffReader.close();
         this.openSchema(loadedSchema);
         this.currentFile = fileToLoad;
         this.currentDirectory = fileToLoad.getParentFile();
         this.addFileToLastFileList(fileToLoad);
         this.programData.checkSchemaItems(loadedSchema);
      } catch (IOException var6) {
         JOptionPane.showMessageDialog(this, "Erreur durant l'ouverture du fichier :\n" + var6.toString(), "Erreur", 0);
         var6.printStackTrace();
      }

   }

   protected void saveAsSchema() {
      this.currentFile = null;
      this.saveSchema();
   }

   protected void buildMenu() {
      JMenuBar menuBar = new JMenuBar();
      JMenu file = new JMenu("Fichier");
      JMenuItem newItem = new JMenuItem("Nouveau", IconLoader.getTinyIcon("nouveau"));
      newItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (MainFrame.this.currentSchema != null) {
               int r = JOptionPane.showConfirmDialog(MainFrame.this, "Un schéma est déjà ouvert, voulez-vous l'enregistrer avant d'en ouvrir un autre?", "Attention", 1, 2);
               if (r == 0) {
                  if (!MainFrame.this.saveSchema()) {
                     return;
                  }
               } else if (r != 1) {
                  return;
               }
            }

            MainFrame.this.currentFile = null;
            MainFrame.this.openSchema(new Schema());
         }
      });
      file.add(newItem);
      JMenuItem load = new JMenuItem("Ouvrir");
      load.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            MainFrame.this.loadSchema((File)null);
         }
      });
      file.add(load);
      this.lastFilesMenu = new JMenu("Fichiers récents");
      this.updateLastFilesMenu();
      file.add(this.lastFilesMenu);
      file.addSeparator();
      JMenuItem save = new JMenuItem("Enregister", IconLoader.getTinyIcon("enregistrer"));
      save.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            MainFrame.this.saveSchema();
         }
      });
      file.add(save);
      JMenuItem saveAs = new JMenuItem("Enregister sous", IconLoader.getTinyIcon("enregistrer_sous"));
      saveAs.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            MainFrame.this.saveAsSchema();
         }
      });
      file.add(saveAs);
      JMenu export = new JMenu("Exporter");
      JMenuItem exportStep = new JMenuItem("Etape courante");
      exportStep.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (MainFrame.this.currentPanel != null) {
               MainFrame.this.currentPanel.exportCurrentStepToPicture((File)null);
            }

         }
      });
      export.add(exportStep);
      JMenuItem exportAllStep = new JMenuItem("Toutes les étapes");
      exportAllStep.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (MainFrame.this.currentPanel != null) {
               MainFrame.this.currentPanel.exportAllSteps();
            }

         }
      });
      export.add(exportAllStep);
      JMenuItem exportLastStep = new JMenuItem("Etat final");
      exportLastStep.setToolTipText("Permet de générer une image où tous les sommets sont présents sans qu'aucun ne soit mis en avant");
      exportLastStep.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (MainFrame.this.currentPanel != null) {
               MainFrame.this.currentPanel.exportLastStep((File)null);
            }

         }
      });
      export.add(exportLastStep);
      file.add(export);
      file.addSeparator();
      JMenuItem exit = new JMenuItem("Quitter", IconLoader.getTinyIcon("close"));
      exit.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            MainFrame.this.saveAndExit();
         }
      });
      file.add(exit);
      menuBar.add(file);
      menuBar.add(this.buildOptionMenu());
      menuBar.add(this.buildHelpMenu());
      this.setJMenuBar(menuBar);
   }

   protected JMenu buildOptionMenu() {
      JMenu option = new JMenu("Options");
      final JCheckBoxMenuItem first = new JCheckBoxMenuItem("Nouveaux sommets à la première étape");
      first.setToolTipText("<html>Cette option permet de toujours faire apparaitre les nouveaux sommets à la première étape.<br>Lorsqu'elle n'est pas activé, les nouveaux sommets apparaitront à l'étape courante.</html>");
      first.setSelected(this.programData.isElementAddedToFirstStep());
      first.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            MainFrame.this.programData.setElementAddedToFirstStep(first.isSelected());
         }
      });
      option.add(first);
      final JCheckBoxMenuItem highlight = new JCheckBoxMenuItem("Mise en valeur des sommets à l'apparition");
      highlight.setSelected(this.programData.isHighlightVertices());
      highlight.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            MainFrame.this.programData.setHighlightVertices(highlight.isSelected());
            if (MainFrame.this.currentPanel != null) {
               MainFrame.this.currentPanel.setEnabledVertexHighlight(highlight.isSelected());
            }

         }
      });
      option.add(highlight);
      option.addSeparator();
      JMenuItem cleanStep = new JMenuItem("Supprimer les étapes inutiles");
      cleanStep.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (MainFrame.this.currentSchema != null) {
               MainFrame.this.currentSchema.cleanStep();
               if (MainFrame.this.currentPanel != null) {
                  MainFrame.this.currentPanel.getControllerPanel().updateStepLabel();
                  MainFrame.this.currentPanel.repaint();
               }
            }

         }
      });
      cleanStep.setToolTipText("Permet de supprimer automatiquement les étapes durant lesquelles aucun sommet n'apparait");
      option.add(cleanStep);
      return option;
   }

   protected JMenu buildHelpMenu() {
      JMenu result = new JMenu("Aide");
      JMenuItem commands = new JMenuItem("Commandes");
      commands.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(MainFrame.this, "Commandes : \n" +
                    "- Shift + Clic : Ajout d'un nouvel élément\n" +
                    "- Ctrl + Clic continu sur un sommet : Déplacement d'un élément\n" +
                    "- Clic sur un sommet : Sélection d'un élément\n" +
                    "- Clic continu d'un élément à l'autre : Ajout et sélection d'une relation\n" +
                    "- Pour ajouter une boucle, appuyer sur Shift avant de lacher le clic de la souris\n" +
                    "- Ctrl + clic en dehors d'un sommet : Déplcament du schéma", "Commandes", 1);
         }
      });
      JMenuItem apparitionStep = new JMenuItem("Etape d'apparition");
      apparitionStep.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(MainFrame.this, "Il est possible de définir un ordre d'apparition des différents éléments d'un schéma.\nPour cela, il suffit de régler la valeur étape de chaque élément. Cette valeur indique à partir de quelle étape sont affichés les sommets\nLes boutons sous le titre du schéma permettent de modifier l'étape actuellement affichée.\nLes boutons + et - permettent de respectivement ajouter une étape après l'étape courante (pour le +)\net de fusionner l'étape courante avec l'étape suivante (pour supprimer l'étape courante).\nL'option Supprimer les étapes inutiles permet de supprimer automatiquement toutes les étapes durant lesquelles aucun élément n'apparaît.\nPar défaut, l'étape d'apparition d'un nouveau sommet est automatiquement définie à l'étape courante,\nl'option \"Nouveaux sommets à la première étape\" permet de toujours les faire apparaitre dès la première étape.\nPar défaut, les sommets apparaissant à une étape sont mis en valeur, il est possible de désactiver cet effet\nà l'aide de l'option \"Mise en valeur des nouveaux à l'apparition.", "Ordre d'apparition", 1);
         }
      });
      JMenuItem export = new JMenuItem("Exporter (génération d'images)");
      export.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(MainFrame.this, "Fonction exporter :\nPermet de générer des images du schéma au format PNG.\nTrois type d'exportations sont possibles : \n-Exporter l'étape courante : Génère une image correspondant à l'étape courante\n-Exporter toutes les étapes : Génère une image pour chaque étape et une image de l'état final\n-Exporter l'état final : Génère une image de l'état final du schéma\nL'état final correspond à l'affichage de tous les sommets sans qu'aucun d'eux ne soit mis en valeur.", "Fonction Exporter", 1);
         }
      });
      JMenuItem credits = new JMenuItem("A propos");
      credits.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(MainFrame.this, "TriaSketch\nVersion alpha\nMaurin NADAL, Robert MICHIT\n30 Septembre 2013");
         }
      });
      result.add(commands);
      result.add(apparitionStep);
      result.add(export);
      result.addSeparator();
      result.add(credits);
      return result;
   }

   private void addFileToLastFileList(File newFile) {
      this.programData.addFile(newFile);
      this.updateLastFilesMenu();
   }

   private void updateLastFilesMenu() {
      this.lastFilesMenu.removeAll();
      Iterator var2 = this.programData.getPrecedentFiles().iterator();

      while(var2.hasNext()) {
         File f = (File)var2.next();
         JMenuItem item = new JMenuItem(f.getName());
         final File goodFile = new File(f.getPath());
         item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               MainFrame.this.loadSchema(goodFile);
            }
         });
         this.lastFilesMenu.add(item);
      }

      if (this.programData.getPrecedentFiles().isEmpty()) {
         this.lastFilesMenu.setEnabled(false);
      } else {
         this.lastFilesMenu.setEnabled(true);
      }

   }

   public void saveAndExit() {
      try {
         if (this.currentSchema != null) {
            int r = JOptionPane.showConfirmDialog(this, "Voulez-vous enregistrer le schéma avant de quitter?", "Demande de confirmation", 1, 3);
            if (r == 0) {
               if (!this.saveSchema()) {
                  return;
               }
            } else if (r != 1) {
               return;
            }
         }
      } catch (Exception var3) {
         ;
      }

      try {
         this.programData.saveProgramData();
      } catch (Exception var2) {
         ;
      }

      System.exit(0);
   }

   public ProgramData getProgramData() {
      return this.programData;
   }

   public File getCurrentFile() {
      return this.currentFile;
   }

   public static void showLicenceDialog() {
      StringBuilder licence = new StringBuilder();
      InputStream iS = MainFrame.class.getResourceAsStream("/licenceFile.txt");
      if (iS == null) {
         JOptionPane.showMessageDialog((Component)null, "Erreur durant l'ouverture du fichier de licence", "Licence", 2);
      } else {
         BufferedReader reader = new BufferedReader(new InputStreamReader(iS));

         String line;
         try {
            while((line = reader.readLine()) != null) {
               licence.append(line);
               licence.append("\n");
            }
         } catch (IOException var8) {
            var8.printStackTrace();
         }

         JTextArea licenceArea = new JTextArea(licence.toString(), 25, 110);
         licenceArea.setEditable(false);
         licenceArea.setLineWrap(true);
         licenceArea.setWrapStyleWord(true);
         JScrollPane scrollPane = new JScrollPane(licenceArea);
         String[] choice = new String[]{"J'accepte", "Je refuse"};
         int r = JOptionPane.showOptionDialog((Component)null, scrollPane, "Licence d'utilisation", 0, 2, IconLoader.getTinyIcon("userRed"), choice, choice[1]);
         if (r != 0) {
            System.exit(0);
         }

      }
   }
}
