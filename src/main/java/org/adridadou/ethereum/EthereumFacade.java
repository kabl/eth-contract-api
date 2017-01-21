package org.adridadou.ethereum;

import static java.lang.reflect.Proxy.newProxyInstance;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.common.base.Charsets;
import org.adridadou.ethereum.blockchain.EthereumProxy;
import org.adridadou.ethereum.converters.input.InputTypeConverter;
import org.adridadou.ethereum.converters.input.InputTypeHandler;
import org.adridadou.ethereum.converters.output.OutputTypeConverter;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.swarm.SwarmHash;
import org.adridadou.ethereum.swarm.SwarmService;
import org.adridadou.ethereum.values.*;
import org.adridadou.ethereum.values.smartcontract.SmartContractMetadata;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import rx.Observable;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumFacade {
    public final static Charset CHARSET = Charsets.UTF_8;
  private final EthereumContractInvocationHandler handler;
    private final OutputTypeHandler outputTypeHandler;
    private final InputTypeHandler inputTypeHandler;
    private final EthereumProxy ethereumProxy;
    private final SwarmService swarmService;

    public EthereumFacade(EthereumProxy ethereumProxy, InputTypeHandler inputTypeHandler, OutputTypeHandler outputTypeHandler, SwarmService swarmService) {
        this.inputTypeHandler = inputTypeHandler;
        this.outputTypeHandler = outputTypeHandler;
        this.swarmService = swarmService;
        this.handler = new EthereumContractInvocationHandler(ethereumProxy, inputTypeHandler, outputTypeHandler);
        this.ethereumProxy = ethereumProxy;
    }

    public EthereumFacade addInputHandlers(final List<InputTypeConverter> handlers) {
        inputTypeHandler.addConverters(handlers);
        return this;
    }

    public EthereumFacade addOutputHandlers(final List<OutputTypeConverter> handlers) {
        outputTypeHandler.addConverters(handlers);
        return this;
    }

    public <T> T createContractProxy(CompiledContract contract, EthAddress address, EthAccount account, Class<T> contractInterface) {
        T proxy = (T) newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
        handler.register(proxy, contractInterface, compile(contract.getSource(), contract.getName()), address, account);
        return proxy;
    }

    public <T> T createContractProxy(ContractAbi abi, EthAddress address, EthAccount account, Class<T> contractInterface) {
        T proxy = (T) newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
        handler.register(proxy, contractInterface, abi, address, account);
        return proxy;
    }

    public <T> T createContractProxy(EthAddress address, EthAccount account, Class<T> contractInterface) {
        T proxy = (T) newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
        handler.register(proxy, contractInterface, getAbi(address), address, account);
        return proxy;
    }

    public <T> Builder<T> createContractProxy(EthAddress address, Class<T> contractInterface) {
        return new Builder<>(contractInterface, address, getAbi(address));
    }

    public <T> Builder<T> createContractProxy(EthAddress address,ContractAbi abi, Class<T> contractInterface) {
        return new Builder<>(contractInterface, address, abi);
    }

    public <T> Builder<T> createContractProxy(CompiledContract contract, EthAddress address, Class<T> contractInterface) {
        return new Builder<>(contractInterface, address, contract.getAbi()
        );
    }

    public ContractAbi getAbi(final EthAddress address) {
        SmartContractByteCode code = ethereumProxy.getCode(address);
        SmartContractMetadata metadata = getMetadata(code.getMetadaLink().orElseThrow(() -> new EthereumApiException("no metadata link found for smart contract on address " + address.toString())));
        return metadata.getAbi();
    }

    public CompletableFuture<EthAddress> publishContract(CompiledContract contract, EthAccount sender, Object... constructorArgs) {
        return ethereumProxy.publish(contract, sender, constructorArgs);
    }

    public SwarmHash publishMetadataToSwarm(CompiledContract contract) {
      return swarmService.publish(contract.getMetadata().getValue());
    }

    public boolean addressExists(final EthAddress address) {
        return ethereumProxy.addressExists(address);
    }

    public EthValue getBalance(final EthAddress addr) {
        return ethereumProxy.getBalance(addr);
    }

    public EthValue getBalance(final EthAccount account) {
        return ethereumProxy.getBalance(account.getAddress());
    }

    public EthereumEventHandler events() {
        return ethereumProxy.events();
    }

    public CompletableFuture<EthExecutionResult> sendEther(EthAccount fromAccount, EthAddress to, EthValue value) {
        return ethereumProxy.sendTx(value, EthData.empty(), fromAccount, to);
    }

    public BigInteger getNonce(EthAddress address) {
        return ethereumProxy.getNonce(address);
    }

    public SmartContractByteCode getCode(EthAddress address) {
        return ethereumProxy.getCode(address);
    }

    public SmartContractMetadata getMetadata(SwarmMetadaLink swarmMetadaLink) {
      try {
        return swarmService.getMetadata(swarmMetadaLink.getHash());
      } catch (IOException e) {
        throw new EthereumApiException("error while getting metadata", e);
      }
    }

    public void shutdown() {
        ethereumProxy.shutdown();
    }

  public CompiledContract compile(SoliditySource src, String contractName) {
      try {
          SolidityCompiler.Result result = SolidityCompiler.getInstance().compileSrc(src.getSource().getBytes(EthereumFacade.CHARSET), true,true,
            SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN, SolidityCompiler.Options.METADATA);
          if (result.isFailed()) {
              throw new EthereumApiException("Contract compilation failed:\n" + result.errors);
          }
          CompilationResult res = CompilationResult.parse(result.output);
          if (res.contracts.isEmpty()) {
              throw new EthereumApiException("Compilation failed, no contracts returned:\n" + result.errors);
          }
          CompilationResult.ContractMetadata metadata = res.contracts.get(contractName);
          if(metadata == null) {
              throw new EthereumApiException("No contract found with the name " + contractName + " available:" + res.contracts.keySet());
          }
          if (metadata.bin == null || metadata.bin.isEmpty()) {
              throw new EthereumApiException("Compilation failed, no binary returned:\n" + result.errors);
          }
          return CompiledContract.from(src, contractName, metadata);
      } catch (IOException e) {
          throw new EthereumApiException("error while compiling " + contractName, e);
      }
  }

    public <T> Observable<T> observeEvents(ContractAbi abi, EthAddress address, String eventName, Class<T> cls) {
        return ethereumProxy.observeEvents(abi, address,eventName, cls);
    }

    public class Builder<T> {

        private final Class<T> contractInterface;
        private final EthAddress address;
        private final ContractAbi abi;

        public Builder(Class<T> contractInterface, EthAddress address, ContractAbi abi) {
            this.contractInterface = contractInterface;
            this.address = address;
            this.abi = abi;
        }

        public T forAccount(final EthAccount account) {
            T proxy = (T) newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
            EthereumFacade.this.handler.register(proxy,contractInterface,abi, address, account);
            return proxy;
        }
    }
}
