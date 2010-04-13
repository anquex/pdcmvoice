% GREENBERG+MLE

clear all;
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
batchsizes=[1024];

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


close all;
hold all;

ii=1;
for T=[10,20,30];
    fT=inline('binopdf(i,c+i+s,p_idle)*binopdf(s,c+s,p_succ_cond_idle)','p_idle','p_succ_cond_idle','n','c','i','s');
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
    
    MLElookup=zeros(slots,T+1,T+1);
    
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
                MLElookup(l,c+1,s+1)=x;
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
                    x=MLElookup(l,c+1,s+1);
                    tmp4par(x)=tmp4par(x)+f(i,l)*fT(MLEp(l),n,c,T-c-s,s);
                end
            end
        end
        E(i,:)=tmp4par;
    end
    
    
    x=7:15;
    h{ii}=plot(x,E(1,x));
    set(h{ii},'MarkerFaceColor',get(h{ii},'Color'));
    set(h{ii},'MarkerSize',4);
    set(h{ii},'MarkerEdgeColor','k');
    ii=ii+1;
    
end

grid on;

set(h{1}, 'Marker', 'o');
set(h{2}, 'Marker', 's');
set(h{3}, 'Marker', 'd','MarkerSize',5);
legend('T=10','T=20','T=30','Location','NorthWest');
hx=xlabel('$\log_{\ 2}\hat{n}$, (slot)','Interpreter','Latex');
hy=ylabel('Estimate Probability','Interpreter','Latex');
set(hx, 'FontSize', 12);
set(hy, 'FontSize', 12);
fh=figure(1);
saveas(fh,['greenberg-mle-fixed-n-varying-T' int2str(T)], 'epsc');