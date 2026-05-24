package com.bytebridges.anytop.domain.eloadengine;

import com.bytebridges.anytop.domain.transaction.enums.TxnStatus;

public interface UssdTopupService {
    TxnStatus topup(Long txId, String port, String password, String mobile, String amount);
}
