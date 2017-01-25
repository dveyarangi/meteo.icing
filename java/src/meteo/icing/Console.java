package meteo.icing;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import meteo.icing.era.DataStamp;

public class Console
{

	public static void main( String [] args)
	{
		Console c = new Console(12);
		c.show();
	}

	private JLabel stampLabel;

	private JProgressBar[] bars;
	private JLabel [] labels;

	public static class ProgressMeter implements IProgressMeter
	{
		Integer id;
		JProgressBar bar;
		JLabel label;
		public ProgressMeter(int id, JProgressBar bar, JLabel label)
		{
			this.id = id;
			this.label = label;
			this.bar = bar;
		}
		class Updater implements Runnable
		{
			public String text;
			public int progress;
		    @Override
		    public void run() {
				bar.setValue( progress );
				bar.setString( text );
		    }
		    public void update(int progress, String text)
		    {
		    	this.progress = progress; this.text = text;
		    	EventQueue.invokeLater(this);
		    }
		  }

		Updater updater = new Updater();
		@Override
		public void setProgress( int value, String label )
		{
			updater.update( value, label);
		}

		@Override
		public Integer bar() { return id; }

	}

	private ProgressMeter meters [];

	public Console( int bars )
	{
		this.bars = new JProgressBar[bars];
		this.labels = new JLabel[bars];
		this.meters = new ProgressMeter[bars];
	}

	public void show()
	{
		JFrame frame = new JFrame("ERA Interim Downloader");

		//2. Optional: What happens when the frame closes?
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//3. Create components and put them in the frame.
		//...create emptyLabel...

		JPanel root = new JPanel();

		frame.getContentPane().add(root, BorderLayout.CENTER);

		root.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		stampLabel = new JLabel();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0; c.gridy = 0;

		root.add(stampLabel, c);

		for(int bidx = 0; bidx < bars.length; bidx ++ )
		{
			JLabel idLabel = new JLabel(""+(bidx+1));
			idLabel.setPreferredSize(new Dimension(20, 10));

			JProgressBar progressBar = bars[bidx] = new JProgressBar();
			progressBar.setStringPainted( true );
			progressBar.setString("Initializing... ");
			JLabel progressLabel = labels[bidx] = new JLabel(" ");

			ProgressMeter meter = meters[bidx] = new ProgressMeter(bidx, progressBar, progressLabel);


			c.weightx = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0; c.gridy = 2*bidx+1;
			root.add(idLabel, c);

			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1; c.gridy = 2*bidx+1;

			root.add(progressBar, c);

			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1; c.gridy = 2*bidx+2; c.gridwidth = 2;

			root.add(progressLabel, c);

/*			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1; c.gridy = 2*bidx+2; c.gridwidth = 2;

			root.add(new JLabel("---"), c);*/
		}

		stampLabel = new JLabel("---");

		//4. Size the frame.
		frame.pack();

		//5. Show it.
		frame.setVisible(true);
	}

	public void setStamp(DataStamp stamp)
	{
		this.stampLabel.setText(stamp.toString());
	}

	public void setProgress(int bar, int progress, String label)
	{
		JProgressBar progressBar = bars[bar];
		JLabel progressLabel = labels[bar];

		progressBar.setValue(progress);
		progressLabel.setText( label );
	}

	public IProgressMeter getProgressMeter( Integer bar )
	{
		return meters[bar];
	}
}
