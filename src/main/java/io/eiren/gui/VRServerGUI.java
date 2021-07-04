package io.eiren.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;

import io.eiren.util.ann.AWTThread;
import io.eiren.vr.VRServer;
import io.eiren.vr.bridge.NamedPipeVRBridge;

import java.awt.event.MouseEvent;
import java.util.TimerTask;

import static javax.swing.BoxLayout.PAGE_AXIS;
import static javax.swing.BoxLayout.LINE_AXIS;

public class VRServerGUI extends JFrame {
	
	public final VRServer server;
	private final TrackersList trackersList;
	private final SkeletonList skeletonList;
	private java.util.Timer timer = new java.util.Timer();
	private JButton resetButton;
	private JScrollPane scroll;
	private EJBox pane;
	
	@AWTThread
	public VRServerGUI(VRServer server) {
		super("SlimeVR Server");
		increaseFontSize();
		
		this.server = server;
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(new BoxLayout(getContentPane(), PAGE_AXIS));
		
		this.trackersList = new TrackersList(server, this);
		this.skeletonList = new SkeletonList(server, this);
		
		add(scroll = new JScrollPane(pane = new EJBox(PAGE_AXIS), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		
		build();
	}
	
	public void refresh() {
		// Pack and display
		pack();
		setVisible(true);
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				toFront();
				repaint();
			}
		});
	}
	
	@AWTThread
	private void build() {
		pane.removeAll();
		
		NamedPipeVRBridge npvb = server.getVRBridge(NamedPipeVRBridge.class);
		
		pane.add(new EJBox(LINE_AXIS) {{
			setBorder(new EmptyBorder(i(5)));
			
			add(Box.createHorizontalGlue());
			add(resetButton = new JButton("RESET") {{
				addMouseListener(new MouseInputAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						reset();
					}
				});
			}});
			add(Box.createHorizontalGlue());
			if(npvb != null) {
				add(new JButton(npvb.isOneTrackerMode() ? "1" : "3") {{
					addMouseListener(new MouseInputAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							npvb.setSpawnOneTracker(!npvb.isOneTrackerMode());
							setText(npvb.isOneTrackerMode() ? "1" : "3");
						}
					});
				}});
				add(Box.createHorizontalStrut(10));
			}
		}});
		
		pane.add(new EJBox(LINE_AXIS) {{
			setBorder(new EmptyBorder(i(5)));
			add(new EJBox(PAGE_AXIS) {{
				setAlignmentY(TOP_ALIGNMENT);
				
				add(new JLabel("Trackers"));
				add(trackersList);
				add(Box.createVerticalGlue());
			}});

			add(new EJBox(PAGE_AXIS) {{
				setAlignmentY(TOP_ALIGNMENT);
				add(new JLabel("Skeleton"));
				add(skeletonList);
				add(new JLabel("Skeleton config"));
				add(new SkeletonConfig(server, VRServerGUI.this));
				add(Box.createVerticalGlue());
			}});
		}});
		
		refresh();
		setLocationRelativeTo(null);
		
		server.addOnTick(trackersList::updateTrackers);
		server.addOnTick(skeletonList::updateBones);
	}
	
	private static void increaseFontSize() {
		java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
		while(keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if(value instanceof javax.swing.plaf.FontUIResource) {
				javax.swing.plaf.FontUIResource f = (javax.swing.plaf.FontUIResource) value;
				javax.swing.plaf.FontUIResource f2 = new javax.swing.plaf.FontUIResource(f.deriveFont(f.getSize() * 2f));
				UIManager.put(key, f2);
			}
		}
	}
	
	@AWTThread
	private void reset() {
		resetButton.setText("5");
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				resetButton.setText("4");
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						resetButton.setText("3");
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								resetButton.setText("2");
								timer.schedule(new TimerTask() {
									@Override
									public void run() {
										resetButton.setText("1");
										timer.schedule(new TimerTask() {
											@Override
											public void run() {
												server.resetTrackers();
												resetButton.setText("RESET");
											}
										}, 1000);
									}
								}, 1000);
							}
						}, 1000);
					}
				}, 1000);
			}
		}, 1000);
	}
}
