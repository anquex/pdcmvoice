% EBT EBT+MBT Analysis
clc;

if(matlabpool('size') == 0)
    maxNumCompThreads(2);
    matlabpool local 2;
end;

tic;
nmax=1500;
%costo con MBT sub-ottimo
p=0.5;
Q=zeros(nmax+1); % Q(i+1,n+1) <- Q_i(n)
disp('Inizio calcolo LMBTso');
for n=0:nmax
    tmp=zeros(1,nmax+1);
    parfor i=0:n
        tmp(i+1)=binopdf(i,n,p);
    end
    Q(:,n+1)=tmp;
end

LMBTso=zeros(nmax+1,1);
LMBTso(1)=1;
LMBTso(2)=1;

for n=2:nmax
    s=0;
    for i=1:n-1
        s=s+Q(i+1,n+1)*( LMBTso(i+1)+LMBTso(n-i+1) );
    end
    LMBTso(n+1)=(1+Q(n+1,n+1)+s)/(1-Q(0+1,n+1)-Q(n+1,n+1));
end

disp('LMBTso Calcolato');
toc;
tic;

%costo con MBT ottimo
p=0.4175;
Q=zeros(nmax+1); % Q(i+1,n+1) <- Q_i(n)

disp('Inizio calcolo LMBTo');
for n=0:nmax
    tmp=zeros(1,nmax+1);
    parfor i=0:n
        tmp(i+1)=binopdf(i,n,p);
    end
    Q(:,n+1)=tmp;
end

LMBTo=zeros(nmax+1,1);
LMBTo(1)=1;
LMBTo(2)=1;

for n=2:nmax
    s=0;
    for i=1:n-1
        s=s+Q(i+1,n+1)*( LMBTo(i+1)+LMBTo(n-i+1) );
    end
    LMBTo(n+1)=(1+Q(n+1,n+1)+s)/(1-Q(0+1,n+1)-Q(n+1,n+1));
end

disp('LMBTo Calcolato');
toc;
tic;
batchsizes=2:nmax;
Co=zeros(length(batchsizes),1);
Cso=zeros(length(batchsizes),1);

disp('Inizio Calcolo Miglioramento');
parfor i=1:length(batchsizes);
    n=batchsizes(i);
    m=floor(log2(n));
    p=1/2^m;
    cost=0;
    k=0;
    bin=BINOPDF(k,n,p);
    while (bin>10^-12)
        Co(i)=Co(i)+LMBTo(k+1)*bin;
        Cso(i)=Cso(i)+LMBTso(k+1)*bin;
        k=k+1;
        bin=binopdf(k,n,p);
    end
    Co(i)=Co(i)*m;
    Cso(i)=Cso(i)*m;
end
disp('Miglioramento Calcolato');
toc;
plot(batchsizes,(Cso./Co-1)*100,'b');

grid on
set(gca,'Xtick',[64,128,254,512,1024,nmax])
set(gca,'XMinorTick','on');

set(gca,'Box','off');
hx=xlabel('Batch Size','Interpreter','Latex');
hy=ylabel('Expected Performance Improvement in \%','Interpreter','Latex');
set(hx, 'FontSize', 12);
set(hy, 'FontSize', 12);
title('Comparison of EBT vs EBT with optimal MBT','Interpreter','Latex','FontSize', 14)
set(fh, 'color', 'white'); % sets the color to white
fh=figure(1);
saveas(fh, 'EBT-EBT_MBT-Comparison', 'epsc');

disp('Media');
sum((Cso./Co-1)*100)/length(Cso)