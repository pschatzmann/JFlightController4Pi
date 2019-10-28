package ch.pschatzmann.jflightcontroller4pi.tests;

import org.junit.Assert;
import org.junit.Test;

import ch.pschatzmann.jflightcontroller4pi.data.Data;

public class TestData {
	Data data = new Data();

	@Test
	public void testCRLF() {
		data.setBytes("12345\n67".getBytes());
		Assert.assertEquals(5, data.length());
		Assert.assertEquals('\n',data.getBytes()[data.length()]);
		Assert.assertEquals("12345", data.toString());

		data.setBytes("12345\r67".getBytes());
		Assert.assertEquals("12345", data.toString());
		Assert.assertEquals('\n',data.getBytes()[data.length()]);
	}
	
	@Test
	public void testAppendBytes() {
		data.setBytes("1234".getBytes());
		data.append("5\n67".getBytes());
		Assert.assertEquals("12345", data.toString());
		Assert.assertEquals('\n',data.getBytes()[data.length()]);
	}
	
	@Test
	public void testAppendString() {
		data.setBytes("1234".getBytes());
		data.append("5\n67");
		Assert.assertEquals("12345", data.toString());
		Assert.assertEquals('\n',data.getBytes()[data.length()]);
	}
	
	@Test
	public void testDouble() {
		data.setBytes("1,2,3,4".getBytes());
		Assert.assertEquals(4, data.splitDouble(',',4).length);

		data.setBytes("1, 2, 3, 4".getBytes());
		Assert.assertEquals(4, data.splitDouble(',',4).length);

		data.setBytes(" 1 ,  2 ,  3 , 4 ".getBytes());
		Assert.assertEquals(4, data.splitDouble(',',4).length);
	}
	
	@Test
	public void testEmpty() {
		data.setBytes("       ".getBytes());
		Assert.assertEquals("", data.toString());
		Assert.assertTrue(data.isEmpty());
		Assert.assertEquals(0, data.length());

		data.setBytes("   \n67 ".getBytes());
		Assert.assertEquals("", data.toString());
		Assert.assertTrue(data.isEmpty());
		Assert.assertEquals(0, data.length());

		data.setBytes("                                   ".getBytes());
		Assert.assertEquals("", data.toString());
		Assert.assertTrue(data.isEmpty());
		Assert.assertEquals(0, data.length());

		data.setBytes("                                           ".getBytes());
		Assert.assertEquals("", data.toString());
		Assert.assertTrue(data.isEmpty());
		Assert.assertEquals(0, data.length());


	}

}
