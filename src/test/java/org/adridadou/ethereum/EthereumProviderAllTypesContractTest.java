package org.adridadou.ethereum;

import org.adridadou.ethereum.converters.input.InputTypeHandler;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.ethj.EthereumTest;
import org.adridadou.ethereum.ethj.TestConfig;
import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.swarm.SwarmService;
import org.adridadou.ethereum.values.*;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EthereumProviderAllTypesContractTest {
    private final EthereumTest ethereumj = new EthereumTest(TestConfig.builder().build());
    private final InputTypeHandler inputTypeHandler = new InputTypeHandler();
    private final OutputTypeHandler outputTypeHandler = new OutputTypeHandler();
    private final EthereumEventHandler handler = new EthereumEventHandler();
    private final EthereumProxy bcProxy = new EthereumProxy(ethereumj, handler, inputTypeHandler, outputTypeHandler);
    private final EthAccount account = ethereumj.defaultAccount();
    private final EthereumFacade ethereum = new EthereumFacade(bcProxy, inputTypeHandler, outputTypeHandler, SwarmService.from(SwarmService.PUBLIC_HOST), SolidityCompiler.getInstance());

    @Before
    public void before() {
        handler.onReady();
    }

    @Test
    public void checkSuccessCase() throws IOException, ExecutionException, InterruptedException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("allTypesContract.sol").getFile());

        SoliditySource contractSource = SoliditySourceFile.from(file);
        CompiledContract compiledContract = ethereum.compile(contractSource).get().get("allTypesContract");
        EthAddress address = ethereum.publishContract(compiledContract, account).get();

        AllTypesContract proxy = ethereum.createContractProxy(compiledContract, address, account, AllTypesContract.class);

        proxy.setString("ok").get();
        assertThat(proxy.getString(), is("ok"));

        proxy.setUint(123).get();
        assertThat(proxy.getUint(), is(123));

        proxy.setInt(123).get();
        assertThat(proxy.getInt(), is(123));

        proxy.setAddress(account.getAddress()).get();
        assertThat(proxy.getAddress(), is(account.getAddress()));


        proxy.setBool(true).get();
        assertTrue(proxy.getBool());

        EthData data = EthData.of("ok".getBytes());
        proxy.setBytes32("UUUU".getBytes()).get();
        byte[] actualData = proxy.getBytes32();
        assertThat(actualData, is(data));
    }

    private interface AllTypesContract {
        CompletableFuture<Void> setString(String data);
        String getString();

        CompletableFuture<Void> setUint(int data);
        int getUint();

        CompletableFuture<Void> setInt(int data);
        int getInt();

        CompletableFuture<Void> setAddress(EthAddress address);
        EthAddress getAddress();

        CompletableFuture<Void> setBool(boolean data);
        boolean getBool();

        CompletableFuture<Void> setBytes32(byte[] data);
        byte[] getBytes32();
    }
}
