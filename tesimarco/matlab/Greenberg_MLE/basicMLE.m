% GREENBERG+MLE

clear all;
close all;
clc;

%{
if(matlabpool('size') == 0)
    maxNumCompThreads(2);
    matlabpool local 2;
end;
%}

% prob_succ(p,n)
% prob_idle(p,n)
% prob_coll(p,n)
% prob_succ_cond_idle(p,n)

% batchsizes per il test
batchsizes=[1024,2048,4096];

% MLE batchsizes
b=2;
x=1:16;
MLEbs=b.^x;
clear x;

slots=log2(max(MLEbs))+6;
MLEp=1./b.^(1:slots);

% calcolo tabella di fine
fMLE=prob_fine(MLEbs,MLEp,slots);


% fT(l,n,c,i,s)
fT=inline('nchoosek(c+i+s,i).*(prob_idle(p,n)).^i.*(1-prob_idle(p,n)).^(c+s)    .*   nchoosek(c+s,s).*(prob_succ_cond_idle(p,n)).^s.*(1-prob_succ_cond_idle(p,n)).^c','p','n','c','i','s');

T=20;
MLEraw=zeros(slots,T,T,length(MLEbs));

for l=1:slots
    p=MLEp(l);
    for x=1:length(MLEbs)
        n=MLEbs(x);
        for s=0:T
            for c=0:T-s
                i=T-c-s;
                % uniforme
                % MLEraw(l,c+1,s+1,x)=f(l,n,c,i,s);
                % pesata
                MLEraw(l,c+1,s+1,x)=fT(p,n,c,i,s)*fMLE(x,l);
            end
        end
    end
end

MLElookup=zeros(slots,T,T);

for l=1:slots
    for s=0:T
        for c=0:T-s
            % l'indice restituito coincide con lo slot di trasmissione
            maximum=max(MLEraw(l,c+1,s+1,:));
            x=find(MLEraw(l,c+1,s+1,:)==maximum);
            if(maximum==0)
                error('c'' qualche errore'); %not valid 
            end
            MLElookup(l,c+1,s+1)=x;
        end
    end
end

f=prob_fine(batchsizes,MLEp,slots);

E=zeros(length(batchsizes),slots);

%fT (i, s, c, p(l), n),
%fT(p,n,c,i,s)
for i=1:length(batchsizes)
    n=batchsizes(i);
    %distribuzione taglia n
    for l=1:slots
        for s=0:T
            for c=0:T-s
                x=MLElookup(l,c+1,s+1);
                E(i,x)=E(i,x)+f(i,l)*fT(MLEp(l),n,c,T-c-s,s);
            end
        end 
    end
end

close all;
hold all;
x=7:15;
for i=1:length(batchsizes)
    plot(x,E(i,x))
end
grid on;
legend('1024','2048','4096','Location','NorthWest');
xlabel('$\log_{\ 2}\hat{n}$','Interpreter','Latex');
ylabel('probability','Interpreter','Latex');
fh=figure(1);
saveas(fh,['greenberg-mle-T' int2str(T)], 'epsc');
