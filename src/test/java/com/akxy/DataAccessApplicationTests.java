package com.akxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

public class DataAccessApplicationTests {

	@Test
	public void contextLoads() {

		File file = new File("org.apache.zookeeper.server.quorum.QuorumPeerMain");
		System.out.println(file.getName());
	}

}
