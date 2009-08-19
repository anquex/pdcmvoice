


x=[1:10:1024];

esponente=[5:1:floor(log2(x(end)))];
asse=2*ones(1,length(esponente));
asse=asse.^esponente;

f1=inline('x.*log(x)');
f2=inline('x+x/2.*log(x/2)');
plot(x,f1(x),'r',x,f2(x),'b');

xlabel('# elementi');
ylabel('Operazioni');
set(gca,'XTick',asse)
legend('O(N log(N))','O(N+N/2 log(N/2))','Location','SouthEast');