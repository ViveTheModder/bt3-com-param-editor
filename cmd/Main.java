package cmd;
//BT3 COM Param Editor - Main
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main 
{
	public static boolean isForAll=true, isCmrFile=false, isForWii=false;
	public static int health, transformType;
	private static final short[] BYTE_SET_1 = {0,0x280a,0x2803,0x1e32,0x1414,0x1428};
	private static final short[] BYTE_SET_2 = {0,0x280f,0x3c03,0x283c,0x1e1e,0x1e32};
	public static boolean isValidCharaCostume(File src) throws IOException
	{
		boolean error=false;
		RandomAccessFile raf = new RandomAccessFile(src,"r");
		long header = raf.readLong();
		raf.seek(1012);
		int actualSize = (int) raf.length();
		int expectedSize = LittleEndian.getInt(raf.readInt());
		long padding = raf.readLong();
		if (header!=0xFC00000000040000L) error=true;
		if (actualSize!=expectedSize) error=true;
		if (padding!=0) error=true;
		raf.close();
		return error;
	}
	private static void writeComParams(File src) throws IOException
	{
		int comParamAddr=0;
		RandomAccessFile raf = new RandomAccessFile(src,"rw");
		if (!isCmrFile) //skip these instructions if CMR file is being read instead
		{
			raf.seek(108); //go to offset
			comParamAddr = LittleEndian.getInt(raf.readInt()); //get address (int stored in byte 108)
			raf.seek(comParamAddr); //go to address
		}
		for (int i=0; i<512; i++)
		{
			long pos = raf.getFilePointer();
			if (pos-comParamAddr==15) //change first set of bytes
			{
				short byteSet = BYTE_SET_1[transformType];
				if (!isForAll)
				{
					short temp = raf.readShort(); i-=2;
					if (byteSet!=temp)
					{		
						raf.close();
						return;
					}
					raf.seek(pos);
				}
				raf.writeShort(byteSet); i+=2;
			}
			if (pos-comParamAddr==233) //make COM recognize all transformations
			{
				raf.writeByte(10); i++;
			}
			if (pos-comParamAddr==263) //change second set of bytes
			{
				short byteSet = BYTE_SET_2[transformType];
				raf.writeShort(byteSet); i+=2;
				if (health!=-1)
				{
					raf.seek(pos);
					raf.writeByte(health); i++;
				}
			}
			pos++;
			raf.seek(pos);
		}
		raf.close();
	}
	private static void error(Exception e1)
	{
		File errorLog = new File("errors.log");
		try 
		{
			FileWriter logWriter = new FileWriter(errorLog,true);
			logWriter.append(new SimpleDateFormat("dd-MM-yy-hh-mm-ss").format(new Date())+":\n"+e1.getMessage()+"\n");
			logWriter.close();
		} 
		catch (IOException e2) 
		{
			e2.printStackTrace();
		}
	}
	public static void traverse(File src)
	{
		if (src.isDirectory()) //check folder
		{
			String[] nameArray = src.list();
			if (nameArray!=null)
			{
				//check files and subfolders
				for (String name: nameArray) traverse(new File(src,name));
			}
		}
		else if (src.isFile()) //check file
		{
			try
			{
				String fileName = src.getName();
				String currPath = src.getCanonicalPath();
				if (!isCmrFile)
				{
					if (fileName.endsWith("p.pak") || fileName.endsWith(".unk"))
					{
						App.fileLabel.setText(App.HTML_TEXT+currPath);
						if (Main.isValidCharaCostume(src)) writeComParams(src);
					}
				}
				else if (fileName.endsWith("com_param.cmr")) 
				{
					App.fileLabel.setText(App.HTML_TEXT+currPath);
					writeComParams(src);
				}
			}
			catch (IOException e)
			{
				error(e);
			}
		}
	}
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException 
	{
		if (System.getProperty("os.name").contains("Win")) UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		App.setApp();
	}
}