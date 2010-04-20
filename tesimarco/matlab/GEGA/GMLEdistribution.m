% calcola la distribuzione della stima n^ per ogni n in batchsizes
tic
batchsizes=[5:5:95,100:10:2000];
nmax=400000;
T=30;
load(['GMLE-T-' int2str(T)]); %loads GMLE(c,s,l) T b p slots
return;
D=zeros(length(batchsizes),nmax);
bias=zeros(1,length(batchsizes));

%max(max(max(GMLE))); % nel nostro caso è <2^23

% GENERA LE STIME CONSENTITE
allowed=2.^1:23;
b=[];
for i=1:length(allowed)-1
    b=[b floor(linspace(allowed(i),allowed(i+1),3))];
end
allowed=unique(b);
clear b i;

% RESTRINGI IL CODOMINIO DELLE STIME CONSENTITE
GMLE=GEGA2EGA(GMLE,allowed);

% CALCOLA LA DISTRIBUZIONE PER OGNI BATCHSIZE CONSIDERATA
for k=1:length(batchsizes)
    n=batchsizes(k)
    for l=1:slots
        prob_stop_slot=fG(p,n,l);
        if (prob_stop_slot>10^-10)
            %probabilità dell'evento (i,s,c)
            p_idle=prob_idle(p(l),n);
            p_succ_cond_idle=prob_succ_cond_idle(p(l),n);
            for c=0:T
                for s=0:T-c
                    sc=s+c;
                    i=T-sc;
                    fiscpn=binopdf(i,T,p_idle)*binopdf(s,sc,p_succ_cond_idle);
                    D(k,GMLE(c+1,s+1,l))=D(k,GMLE(c+1,s+1,l))+fiscpn*prob_stop_slot;
                end
            end
        end
    end
    estimates=find(D(k,:)>0);
    limit=find(estimates>5*n,1);
    %plot(estimates(1:limit),D(k,estimates(1:limit)));
    bias(k)=sum(estimates.*D(k,estimates))/n;
end
toc
%save(['GMLE-bias-T' int2str(T)]);
