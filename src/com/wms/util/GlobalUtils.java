/*
 * Global.java
 *
 * Created on July 6, 2005, 12:57 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.wms.util;

/**
 *
 * @author ecb
 */

import java.util.Vector;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;


public class GlobalUtils
{
   	//note: build level is always vrmf(4 digits) plus build increment	
	static final int build_level = 00001; 

  	static private GlobalUtils 		_instance;
	static private Standard_Sort	standard_sort 	= new Standard_Sort();
	static private int				original_column = -1;
	static java.util.Random random_gen 				= new java.util.Random();

    /** Creates a new instance of Global */
    public GlobalUtils()
    {
    }

	public int
	get_build_level()
	{
		return build_level;
	}

	//
	//
	//
	public static GlobalUtils
	getInstance()
	{
	  if (_instance == null)
	  {
		synchronized(GlobalUtils.class)
		{
		  if (_instance == null)
			_instance = new GlobalUtils();
		}
	  }
	  return _instance;
	}

	/*requires TableParser.java
	protected void
	save_table( javax.swing.JFrame frame, javax.swing.JTable table )
	{
		//if( print_options_frame != null )
		//	print_options_frame.dispose();

		//String output = parse_table_to_string( selected, table ).toString();
		Object tmp = (Object)table.getModel();
		if( !(tmp instanceof SortTableModel) )
			return;

		SortTableModel model = (SortTableModel)tmp;
		Vector data = model.get_data();	
		
		Vector headers = new Vector();
		for( int i=0; i < model.getColumnCount(); i++ )
			headers.add( model.getColumnName(i) );
		
		//bail if no data exists
		if( data.size() <= 0 )
			return;


		String output = new TableParser().parse_table( headers, data );
		//System.out.println("----------------------");
		//System.out.println(output);

		javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
		int returnVal = fc.showSaveDialog( frame );

		if ( returnVal == javax.swing.JFileChooser.APPROVE_OPTION )
		{
			File file = fc.getSelectedFile();
			File Path = fc.getCurrentDirectory();
			//System.out.println( "Path=" + file.getAbsolutePath() );

			PrintWriter out = null;
			try
			{
				out = new PrintWriter( new FileWriter(
						file.getAbsolutePath(), false ) );

				out.print( output );
				out.flush();
				out.close();
			}
			catch ( IOException e )
			{
				System.err.println( "I/O Error" );
			}
		}
	}
	*/



	public void
	shift_viewport( javax.swing.JScrollPane scroll, java.awt.Component component )
	{
			scroll.validate();

			java.awt.Point position = scroll.getViewport().getViewPosition();
			int y = (int)component.getHeight() - (int)scroll.getViewport().getExtentSize().getHeight();

			if( y < 0 )
				return;

			position.setLocation( (int)position.getX(), y );
			scroll.getViewport().setViewPosition( position );		
	}


	//note: this was moved to TSMInterfaceManager
	//since it was the only object really using it right now.
	public String
	parse_table_to_string( boolean selected, javax.swing.JTable table )
	{

		StringBuffer sb = new StringBuffer();

		int[] rows = new int[0];
		int selected_count = 0;
		if( selected )
			rows = table.getSelectedRows();



		String line_feed = System.getProperty( "line.separator" );


		int longest_column_header = 0;
		for( int i=0; i < table.getColumnCount(); i++ )
			if( table.getColumnName(i).length() > longest_column_header )
				longest_column_header = table.getColumnName(i).length();

		int columns_char_width =  longest_column_header+5;

		//for 80 char page width
		//int columns_char_width =  (int)80/table.getColumnCount();

		//print column headers
		for( int i=0; i < table.getColumnCount(); i++ )
		{
			int count = table.getColumnName(i).length();
			sb.append( table.getColumnName(i) );
			while( ++count < columns_char_width )
				sb.append( " " );
		}

		sb.append( line_feed );

		//print lines
		for( int x=0; x < table.getColumnCount(); x++ )
		{
			int count = 0;
			while( count++ < table.getColumnName(x).length() )
				sb.append( "-" );
			while( count++ < columns_char_width )
				sb.append( " " );
		}
		sb.append( line_feed );


		//print lines
		for( int y=0; y < table.getRowCount(); y++ )
		{
			//move on to next if not selected
			if( selected
				&&
				selected_count < rows.length
				&&
				y != rows[selected_count] )
				continue;

			for( int z=0; z < table.getColumnCount(); z++ )
			{
				int count = ((String)table.getValueAt( y, z )).length();
				sb.append( (String)table.getValueAt( y, z ) );
				while( ++count < columns_char_width )
					sb.append( " " );
			}

			sb.append( line_feed );
			selected_count++;
		}
		sb.append( line_feed );

		return sb.toString();

	}



	//
	// Standard sort algorithm
	//
	public static void
	do_standard_sort(
				String[] source_array )
	{
		//sort the array		
		java.util.Arrays.sort( source_array, standard_sort );		
	}

private static class Standard_Sort implements java.util.Comparator
{
	public int
	compare(
			Object a, 
			Object b )
	{
		if( a == null || b == null )
		{
			return 0;
		}
		//System.out.println("a class=" + a.getClass().getName());
		//System.out.println("b class=" + b.getClass().getName());
		//System.out.println("Standard_Sort:: A->" +
		//					(String)a + " B->" + (String)b );
	
		
		if( a instanceof javax.swing.AbstractButton ||
			b instanceof javax.swing.AbstractButton )
		{
		}


		// get our strings to compare
		String a_string = ((String)a).trim(). //trim string
							replaceAll( "/", //replace slash
										"" ); // with nothing
		String b_string = ((String)b).trim(). //trim string
							replaceAll( "/", //replace slash
										"" ); // with nothing
	
		//if the strings are equal return 0
		if( a_string.compareTo( b_string ) == 0 )
		{
			return 0;
		}

		// break em' down
		Vector a_vector = build_vector( a_string );
		Vector b_vector = build_vector( b_string );

		//DEBUG
		/*
		for(int i=0; i<a_vector.size(); i++ )
		{
			System.out.println("Standard_Sort:: a_vector i=" + i +
			" string=" + a_vector.get(i));
		}
		for(int i=0; i<b_vector.size(); i++ )
		{
			System.out.println("Standard_Sort:: b_vector i=" + i +
			" string=" + b_vector.get(i));
		}
		*/
		
		
		



		//set the steps to the smallest vector
		int steps = 0;
		if(  a_vector.size() <= b_vector.size() )
		{
			steps = a_vector.size();
		}
		else
		{
			steps = b_vector.size();
		}

		//step thru and compare
		for( int i=0; i < steps; i++ )
		{
			int result = compare_types(  a_vector.get(i),
							b_vector.get(i) );

			// if result is zero 
			// then check next item
			// otherwise we have our answer
			if( result != 0 )
			{
				return result;
			}

			// if you are here and there are no
			// more sections to compare, but one 
			// vector still has remaining elements
			// then the numeric next element wins
			// and the aplha next element looses
			if( i+1 == steps )
			{
				i++;
				if( a_vector.size() > i )
				{// a_vector is larger
					if( a_vector.get(i).getClass().getName().
							compareTo("java.lang.Integer") == 0 )
					{
						return -1;
					}
					else
					{
						return 1;
					}
				}
				else if( b_vector.size() > i )
				{
					if(  b_vector.get(i).getClass().getName().
							compareTo("java.lang.Integer") == 0 )
					{
						return 1;
					}
					else
					{
						return -1;
					}					
				}
			}
		}

		//default -- do nothing
		//if we cannot determine
		//assume they are equal
		return 0;
	}


	//
	// compare object types
	//
	private int
	compare_types(
			Object a_object,
			Object b_object )
	{
			if( (a_object).getClass().getName().compareTo(
					(b_object).getClass().getName() ) == 0 )
			//*************************
			//both are the same type
			//so determine type
			//*************************
			{
				if( (a_object).getClass().getName().compareTo(
						"java.lang.Integer" ) == 0 )
				//*************************
				// both are Integers
				// do numeric comparison
				//*************************
				{
					if( ((Integer)a_object).intValue()
							> ((Integer)b_object).intValue() )
					{
						return 1;
					}else if( ((Integer)a_object).intValue()
							< ((Integer)b_object).intValue() )
					{
						return -1;
					}
					else
					{
						return 0;
					}
				}
				else
				if( (a_object).getClass().getName().compareTo(
						"java.lang.Long" ) == 0 )
				//*************************
				// both are Longs
				// do numeric comparison
				//*************************
				{
					if( ((Long)a_object).intValue()
							> ((Long)b_object).intValue() )
					{
						return 1;
					}else if( ((Long)a_object).intValue()
							< ((Long)b_object).intValue() )
					{
						return -1;
					}
					else
					{
						return 0;
					}
				}
				else
				//*************************
				// both must be Strings
				// do string comparison
				//*************************
				{
					return 	((String)a_object).compareTo(
										(String)b_object );
				}
			}
			//*************************
			//  Numeric values 
			//  override String values
			//*************************
			else if( (a_object).getClass().getName().compareTo(
						"java.lang.Integer" ) == 0 )
			{
				return -1;
			}
			else
			{
				return 1;
			}
	}





	//
	// break it down into a vector
	// containning alphabetic and
	// numeric objects
	//
	private Vector
	build_vector( 
			String source_string )
	{

		// if the string is zero length then
		// give it a space to use for comparison
		if( source_string.compareTo("") == 0 )
		{
			source_string = " ";
		}
		
		
		char[] source_char 			= source_string.toCharArray();

		Vector broke_down_vector	= new Vector();

		for( int i = 0; i < source_char.length; )
		{
			if( !Character.isDigit( source_char[i] ) ||
				Character.isWhitespace( source_char[i] ) )
			{
				String holder = "";
				while( i < source_char.length &&
					   !Character.isDigit( source_char[i] )  )
				{
					holder = holder + source_char[i++];
				}
				broke_down_vector.add( holder );
			}
			
			if( i < source_char.length &&
				Character.isDigit( source_char[i] ) )			
			{
				String holder = "";
				while( i < source_char.length && 
					   Character.isDigit( source_char[i] ) ) 
				{
					holder = holder + source_char[i++];
				}
				broke_down_vector.add(  new Long( Long.parseLong( holder ) ) );
			}
						
		}
		return broke_down_vector;
	}
}



	public void
	set_original_column(
			int original_column )
	{
		this.original_column = original_column;
	}

	public void
	reset_original_column()
	{
		new Standard_Table_Column_Sort().set_col( this.original_column ); 
	}


	//
	// Standard column sort algorithm
	//
	public static void
	do_standard_column_sort(
				Vector source_vector,
				int sort_column,
				boolean ascending )
	{
		GlobalUtils.getInstance().set_original_column( sort_column );
		//System.out.println("Start ------ do_standard_column_sort ------");
		//sort the array		
		java.util.Collections.sort( source_vector,
						new Standard_Table_Column_Sort( sort_column,
														ascending ) );	
		//reset 
		new Standard_Table_Column_Sort().reset_values(); 

	}


	//
	//
	//
	public void
	center_frame( 
			javax.swing.JFrame frame )
	{
		// Center the window
		java.awt.Dimension screenDim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		java.awt.Rectangle winDim = frame.getBounds();
		frame.setLocation( ( screenDim.width - winDim.width ) / 2,
				( screenDim.height - winDim.height ) / 2 );	
	}


private static class 
Standard_Table_Column_Sort
implements java.util.Comparator
{
	private static boolean 	ascending 		= true;
	private static int 		col 			= -1;
    private static int 		priority_count	= 0;

	private Standard_Sort	standard_sort;


	public static void
	reset_values()
	{
		ascending 		= true;
		col 			= -1;
		priority_count	= 0;
	}

	public static void
	set_col( int original_column )
	{
		col = original_column;
		priority_count	= 0;
	}

	public
	Standard_Table_Column_Sort()	
	{
		standard_sort = new Standard_Sort();
	}

	public
	Standard_Table_Column_Sort(	int col,
								boolean ascending )	
	{
		this.ascending 	= ascending;
		this.col 		= col;
		standard_sort = new Standard_Sort();
	}
  public int compare(Object one, Object two) 
  {
	  
//	System.out.println("Standard_Table_Column_Sort::compare" );
    if (one instanceof Vector &&
        two instanceof Vector)
    {
      Vector vOne = (Vector)one;
      Vector vTwo = (Vector)two;
      Object oOne = vOne.elementAt(col);
      Object oTwo = vTwo.elementAt(col);
     
		//System.out.print("Standard_Table_Column_Sort:: A->" + oOne.getClass().getName() + "<-");
		//System.out.println(" B->" + oTwo.getClass().getName() + "<- col=" + col);
			
		//System.out.println("Standard_Table_Column_Sort:: A->" + oOne + "<-" );
		//System.out.println("Standard_Table_Column_Sort:: B->" + oTwo + "<-" );

		int result = 0;
		//System.out.println("compare:: ascending=" + ascending + " class=" + oOne.getClass().getName());
		if (ascending)
		{
			if( oOne instanceof String && oTwo instanceof String )
				result = standard_sort.compare( oOne, oTwo );
			else
				result = do_check_box_sort( String.valueOf( oOne ), String.valueOf( oTwo ) );
		}
		else
		{
			if( oOne instanceof String && oTwo instanceof String )
				result =  standard_sort.compare( oTwo, oOne );
			else
				result = do_check_box_sort( String.valueOf( oTwo ), String.valueOf( oOne ) );
		}
		return result;

  	}
  	return 0;
  }

	private int
	do_check_box_sort( Object one, Object two )
	{
		//TODO: this needs to be fixed
		//System.out.println("ascending=" + ascending + " class=" + one.getClass().getName());

		if( one instanceof javax.swing.JCheckBox )
		{
			if( ((javax.swing.JCheckBox)one).isSelected() 
				&&
				((javax.swing.JCheckBox)two).isSelected() )
				return 0;

			int result = 0;
			if( ((javax.swing.JCheckBox)one).isSelected() )
				result = 1;

			if( ((javax.swing.JCheckBox)two).isSelected() )
				result = -1;

			if( !ascending )
				result = -1*result;
		
			return result;
		}
		
		return 0;
		
	}

	

}
  

	public int
	get_random_int()
	{
		//TODO: might change this, but for now just 
		//keep it positive only
		int number = this.random_gen.nextInt();
		if( number < 0 )
			number = number*-1;

		return number;
	}
    
}
