% GREENBERG+MLE

%clear all;
close all;
clc;


if(matlabpool('size') == 0)
    maxNumCompThreads(2);
    matlabpool local 2;
end;


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

slots=ceil(log(max(MLEbs))/log(b))+6;
MLEp=1./b.^(1:slots);

% calcolo tabella di fine
fMLE=prob_fine(MLEbs,MLEp,slots);


% fT(l,n,c,i,s)
%fT=inline('nchoosek(c+i+s,i).*(prob_idle(p,n)).^i.*(1-prob_idle(p,n)).^(c+s)    .*   nchoosek(c+s,s).*(prob_succ_cond_idle(p,n)).^s.*(1-prob_succ_cond_idle(p,n)).^c','p','n','c','i','s');
fT=inline('binopdf(i,c+i+s,p_idle)*binopdf(s,c+s,p_succ_cond_idle)','p_idle','p_succ_cond_idle','n','c','i','s');

T=20;
MLEraw=zeros(slots,T+1,T+1,length(MLEbs));
tic
parfor l=1:slots
    p=MLEp(l);
    tmp4par=zeros(T+1,T+1,length(MLEbs));
    for x=1:length(MLEbs)
        n=MLEbs(x);
        p_idle=prob_idle(p,n);
        p_succ_cond_idle=prob_succ_cond_idle(p,n);
        computedfMLE=fMLE(x,l);
        for s=0:T
            for c=0:T-s
                i=T-c-s;
                % uniforme
                % MLEraw(l,c+1,s+1,x)=f(l,n,c,i,s);
                % pesata
                
                %tmp4par(c+1,s+1,x)=fT(p,n,c,i,s)*computedfMLE;
                tmp4par(c+1,s+1,x)=fT(p_idle,p_succ_cond_idle,n,c,i,s)*computedfMLE;
            end
        end
    end
    MLEraw(l,:,:,:)=tmp4par;
end
toc

MLElookup=zeros(T+1,T+1,slots);

for l=1:slots
    for s=0:T
        for c=0:T-s
            % l'indice restituito coincide con lo slot di trasmissione
            maximum=max(MLEraw(l,c+1,s+1,:));
            x=find(MLEraw(l,c+1,s+1,:)==maximum);
            %{
            if(maximum==0)
                error('c'' qualche errore'); %not valid 
            end
            %}
            MLElookup(c+1,s+1,l)=x;
        end
    end
end

f=prob_fine(batchsizes,MLEp,slots);

E=zeros(length(batchsizes),slots);

%fT (i, s, c, p(l), n),
%fT(p,n,c,i,s)
fT=inline('nchoosek(c+i+s,i).*(prob_idle(p,n)).^i.*(1-prob_idle(p,n)).^(c+s)    .*   nchoosek(c+s,s).*(prob_succ_cond_idle(p,n)).^s.*(1-prob_succ_cond_idle(p,n)).^c','p','n','c','i','s');

parfor i=1:length(batchsizes)
    n=batchsizes(i);
    tmp4par=zeros(1,slots);
    %distribuzione taglia n
    for l=1:slots
        for s=0:T
            for c=0:T-s
                x=MLElookup(c+1,s+1,l);
                tmp4par(x)=tmp4par(x)+f(i,l)*fT(MLEp(l),n,c,T-c-s,s);
            end
        end 
    end
    E(i,:)=tmp4par;
end

close all;
hold all;
x=7:15;
for i=1:length(batchsizes)
    h{i}=plot(x,E(i,x));
    set(h{i},'MarkerFaceColor',get(h{i},'Color'));
    set(h{i},'MarkerSize',4);
    set(h{i},'MarkerEdgeColor','k');
end
grid on;

set(h{1}, 'Marker', 'o');
set(h{2}, 'Marker', 's');
set(h{3}, 'Marker', 'd','MarkerSize',5);
legend('1024=2^{10}','2048=2^{11}','4096=2^{12}','Location','NorthWest');
hx=xlabel('$\log_{\ 2}\hat{n}$, (slot)','Interpreter','Latex');
hy=ylabel('Estimate Probability','Interpreter','Latex');
set(hx, 'FontSize', 12);
set(hy, 'FontSize', 12);
fh=figure(1);
saveas(fh,['greenberg-mle-T' int2str(T)], 'epsc');