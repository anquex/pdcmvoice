package Recovery;

dim pacchetto
primo SN ricevuto (SN del RTP � lo stesso del pacchetto speex correntericevuto nel RTP)
timestamp iniziale

Se non � la collezione locale: ultimo SN fino a cui ho fatto o sto facendo  richieste TCP (controllo "buchi")

array di oggetti RecoverySample: ogni oggetto RecoverySample � costituito da un SN (16bit) e da un array di byte (il pacchetto speex) lungo "dim pacchetto"

metodo add per Marco: con parametri(SN-da 16 bit-, array di byte, long timestamp)
	se il SN da inserire � precedente a "ultimo SN fino a cui ho fatto..." allora non inserisco

nelle richieste TCP devo richiedere il SN di partenza + offset dato dall'indice dell'array
	ogni richiesta rimane in sosteso finch� non sono arrivati i pckt 