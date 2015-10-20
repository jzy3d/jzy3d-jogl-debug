package org.jzy3d.demos.browser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jogl.demos.Gears;
import net.miginfocom.swing.MigLayout;

import org.jzy3d.demos.browser.JEntityList.IEntityActionListener;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;

/**
 * This demo browser is initially build for jzy3d.
 * 
 * When adding a jzy3d chart with a CanvasNewtAwt embedding a NewtCanvasAWT,
 * it is displayed properly BUT if you magnify and then downsize the frame, 
 * the NewtCanvasAWT will first grow but won't be able to decrease its size.
 * 
 * SEE SCREENSHOTS OF THIS BUG IN images/jzy3dbug
 * 
 * HOWEVER, when removing Jzy3d layer and simply adding a JOGL demo to chartPanel,
 * NewtCanvasAWT (or one of its parent component) appears on top of Miglayout.
 * Which is not what I wanted to demonstrate but still a very annoying thing :)
 * 
 * SEE SCREENSHOTS OF THIS BUG IN images/demobug.on.macos10.8.5
 * 
 * ------------------------------------------------------------------
 * 
 * To summarize component hierarchy:
 * 
 * DemoBrowser (JFrame)
 * - swing.MigLayout (version 3.7.4)
 *   - JPanel holding a list of demo
 *   - JPanel ("chartPanel")
 *     - (awt) Panel that simulate jzy3d CanvasNewtAwt
 *       - NewtCanvasAWT
 *   - JTextArea ("textArea")
 *   
 *   
 */
public class DemoBrowser extends JFrame {
    private static final long serialVersionUID = 7519209038396190502L;

    public static void main(String[] args) throws IOException {
        new DemoBrowser();
    }

    protected Component currentCanvas;
    protected JEntityList<String> demoList;
    protected JPanel chartPanel;
    protected JTextField tf;
    protected JTextArea textArea;
    protected JScrollPane textPane;
    
    public static String WT = "awt";

    public DemoBrowser() throws IOException {
        // Demo list widget
        demoList = new JEntityList<String>(new CellRenderer<String>() {
            @Override
            public String format(String entity) {
                return entity;//entity.getName();
            }
        });
        demoList.addEntityListener(new IEntityActionListener<String>() {
            @Override
            public void entitySelected(String demo) {
                try {
                    showDemo(demo);
                } catch (Exception e) {
                    popup("Info", e.getMessage());
                }
            }
        });

        demoList.add("click me to start demo");
        //List<IAnalysis> demos = DemoList.getDemos(WT);
        //demoList.add(demos);

        // Info area
        textArea = new JTextArea();
        textArea.setFont(new Font("Arial", Font.ITALIC, 10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textPane = new JScrollPane(textArea);
        textPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Chart panel
        chartPanel = new JPanel(new BorderLayout());
        Border b = BorderFactory.createLineBorder(Color.black);
        chartPanel.setBorder(b);


        // Main layout
        String lines = "[500px,grow][150px]";
        String columns = "[250px][500px,grow]";
        setLayout(new MigLayout("", columns, lines));
        
        demoList.setMinimumSize(new Dimension(200,200));

        add(demoList, "cell 0 0, grow");
        add(chartPanel, "cell 1 0, grow");
        add(textPane, "cell 0 1, span, grow");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DemoBrowser.this.dispose();
                System.exit(0);
            }
        });

        this.pack();
        show();
        setVisible(true);   
    }
    
    protected void showDemo(String demo){
        if(chartPanel==null)
            return;
        
        GLEventListener gle = new Gears();
        Panel pseudoJzy3dCanvasNewtAwt = makeJzy3dLikeCanvas(gle);
        
        //currentChart.render(); // i.e. window.display -> not necessary here 
        
        // remove old chart canvas
        if (currentCanvas != null) {
            chartPanel.remove(currentCanvas);
            currentCanvas = null;
        }
        
        currentCanvas = pseudoJzy3dCanvasNewtAwt;
        chartPanel.add(currentCanvas, BorderLayout.CENTER);
        textArea.setText("please magnigy and minify the frame");

        show(); // use this to actually show the demo otherwise nothing drawn
    }

    /** @see org.jzy3d.plot3d.rendering.canvas.CanvasNewtAwt constructor */
    private Panel makeJzy3dLikeCanvas(GLEventListener gle) {
        // --------------------------------------------------------
        // Code below acts like jzy3d
        Panel aPanelLikeJzy3dCanvasNewtAwtBaseClass = new Panel();

        GLProfile glp = GLProfile.get(GLProfile.GL2);
        GLCapabilitiesImmutable gci = new GLCapabilities(glp);
        GLWindow window = GLWindow.create(gci);
        NewtCanvasAWT newtCanvasAwt = new NewtCanvasAWT(window);
        window.addGLEventListener(gle);
        
        setFocusable(true);
        requestFocusInWindow();
        window.setAutoSwapBufferMode(true/*default in jzy3d*/);
        if (true/*default in jzy3d*/) {
            Animator animator = new Animator(window);
            animator.start();
        }

        
        setLayout(new BorderLayout());
        add(newtCanvasAwt, BorderLayout.CENTER);

        // end of constructor in jzy3d
        // --------------------------------------------------------
        
        return aPanelLikeJzy3dCanvasNewtAwtBaseClass;
    }

    /*protected void showDemo(String demo) throws Exception {
    	if(chartPanel==null)
    		return;
    	
        if (currentDemo != null && currentDemo instanceof IRunnableAnalysis)
            ((IRunnableAnalysis) currentDemo).stop();

        currentDemo = demo;

        // Initialize the demo
        if (demo.isInitialized()) {
            demo.getChart().dispose();
        }
        
        //demo.setFactory(new NewtChartComponentFactory());
        //demo.setCanvasType("newt");
        
        try {
            demo.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add the chart to the target panel
        currentChart = demo.getChart();
        
        if(!demo.hasOwnChartControllers()){
        	currentChart.addMouseController();
        	currentChart.addKeyController();
        	currentChart.addScreenshotKeyController();
        }
        
        StringBuilder st = new StringBuilder();
        String info = demo.getPitch();
        st.append(info);
        if(!info.equals("")){
            st.append("\n------------------------------------\n");
        }
        st.append(ChartLauncher.makeInstruction());
        st.append("Canvas:" + demo.getCanvasType());
        textArea.setText(st.toString());

        currentChart.render(); // i.e. window.display

        // remove old chart canvas
        if (currentCanvas != null) {
            chartPanel.remove(currentCanvas);
            currentCanvas = null;
        }
        currentCanvas = (java.awt.Component) currentChart.getCanvas();
        chartPanel.add(currentCanvas, BorderLayout.CENTER);
        // chartPanel.repaint();
        show();

        // Start
        if (demo instanceof IRunnableAnalysis)
            ((IRunnableAnalysis) currentDemo).start();
    }*/
    
    protected void popup(String title, String message){
    	JOptionPane optionPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE, JOptionPane.CLOSED_OPTION);
        JDialog dialog = optionPane.createDialog(title);
        //dialog.setIconImage(IconLibrary.getIcon(Designer.WINDOW_ICON).getImage());
        dialog.show();
    }
}
